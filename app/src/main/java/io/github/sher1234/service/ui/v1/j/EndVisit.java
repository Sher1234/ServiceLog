package io.github.sher1234.service.ui.v1.j;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import io.github.sher1234.service.R;
import io.github.sher1234.service.functions.Common;
import io.github.sher1234.service.functions.TaskVisit;
import io.github.sher1234.service.model.base.Call;
import io.github.sher1234.service.model.base.Visit;
import io.github.sher1234.service.model.response.Responded;
import io.github.sher1234.service.util.Strings;
import io.github.sher1234.service.util.form.listener.OnFormElementValueChange;
import io.github.sher1234.service.util.form.model.FormElement;

public class EndVisit extends AppCompatActivity
        implements OnFormElementValueChange, View.OnClickListener, TaskVisit.TaskUpdate {

    private final Common common = new Common();
    private FloatingActionButton fab;
    private TaskVisit taskV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.d_activity);
        Call call = (Call) getIntent().getSerializableExtra(Strings.ExtraData1);
        Visit visit = (Visit) getIntent().getSerializableExtra(Strings.ExtraData2);
        if (call == null || visit == null) {
            Toast.makeText(this, "Unspecified error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        ((Toolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ((AppCompatTextView) findViewById(R.id.textViewTitle)).setText(R.string.end_visit);
        taskV = TaskVisit.VisitEnd(this, visit, call, this);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);
        setResult(102);
        fab.hide();
    }

    @Override
    public void onValueChanged(FormElement formElement) {
        if (taskV.isFullFormValid()) fab.show();
        else fab.hide();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab) fetch();
    }

    @Override
    public void onFetched(Responded response, int i) {
        common.dismissProgressDialog();
        if (response != null) {
            Snackbar.make(fab, response.Message, Snackbar.LENGTH_SHORT).show();
            if (response.Code == 1) taskV.onVisitEnded(this);
        } else {
            if (i == 306)
                Snackbar.make(fab, "Content parse error", Snackbar.LENGTH_LONG).show();
            else if (i == 307)
                Snackbar.make(fab, "Network failure", Snackbar.LENGTH_LONG).show();
            else if (i == 308)
                Snackbar.make(fab, "Request cancelled", Snackbar.LENGTH_LONG).show();
            taskV.onNetworkError(this, this);
        }
    }

    @Override
    public void onFetch() {
        common.showProgressDialog(this);
        setResult(100);
    }

    @Override
    public void fetch() {
        taskV.onEndVisit(this, taskV.getVisitEnd());
    }
}