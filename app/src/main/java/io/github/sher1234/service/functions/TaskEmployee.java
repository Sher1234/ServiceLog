package io.github.sher1234.service.functions;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import io.github.sher1234.service.AppController;
import io.github.sher1234.service.R;
import io.github.sher1234.service.api.Api;
import io.github.sher1234.service.model.response.Dashboard;
import io.github.sher1234.service.util.MaterialDialog;
import io.github.sher1234.service.util.Strings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class TaskEmployee {

    private Task task;

    public TaskEmployee() {
        task = null;
    }

    public void onRefreshBoard(TaskUpdate taskUpdate, String id) {
        String s = new SimpleDateFormat(Strings.DateTimeServer, Locale.US).format(Calendar.getInstance().getTime());
        if (task != null)
            task.cancel(true);
        task = new Task(taskUpdate, id, s);
        task.execute();
    }

    public void onNetworkError(final FragmentActivity context,
                               final TaskUpdate taskUpdate,
                               final String id) {
        final MaterialDialog dialog = MaterialDialog.Dialog(context);
        dialog.setTitle("Network issue")
                .setDescription(R.string.offline_access)
                .positiveButton("Retry", new MaterialDialog.ButtonClick() {
                    @Override
                    public void onClick(MaterialDialog dialog, View v) {
                        onRefreshBoard(taskUpdate, id);
                        dialog.dismiss();
                    }
                })
                .negativeButton("Exit", new MaterialDialog.ButtonClick() {
                    @Override
                    public void onClick(MaterialDialog dialog, View v) {
                        context.finish();
                        dialog.dismiss();
                    }
                })
                .setCancelable(false);
        dialog.show();
    }

    public interface TaskUpdate {
        void onFetched(@Nullable Dashboard dashboard, int i);

        void onFetch();

        void fetch();
    }

    @SuppressLint("StaticFieldLeak")
    private class Task extends AsyncTask<Void, Void, Boolean> {

        private final TaskUpdate update;
        private final String userId;
        private final String date;

        private Dashboard dashboard;
        private int code = 0;

        Task(TaskUpdate update, String userId, String date) {
            this.userId = userId;
            this.update = update;
            this.date = date;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            update.onFetch();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Retrofit retrofit = AppController.getInstance().getRetrofitRequest(Api.BASE_URL);
            Api api = retrofit.create(Api.class);
            Call<Dashboard> call = api.getUserAdmin(userId, date);
            call.enqueue(new Callback<Dashboard>() {
                @Override
                public void onResponse(@NotNull Call<Dashboard> call, @NotNull Response<Dashboard> response) {
                    if (response.body() != null) {
                        dashboard = response.body();
                        code = dashboard.code;
                    } else {
                        dashboard = null;
                        code = 306;
                    }
                }

                @Override
                public void onFailure(@NotNull Call<Dashboard> call, @NotNull Throwable t) {
                    t.printStackTrace();
                    dashboard = null;
                    code = 307;
                }
            });
            while (true) {
                if (isCancelled()) {
                    dashboard = null;
                    code = 308;
                    return true;
                }
                if (code != 0)
                    return true;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            update.onFetched(dashboard, code);
        }
    }
}