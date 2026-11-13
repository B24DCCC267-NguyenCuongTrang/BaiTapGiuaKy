package com.example.bitpgiak;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    private TextView tvStatTotal, tvStatCompleted, tvStatPending, tvEmpty;
    private EditText etSearch;
    private Spinner spFilterStatus, spFilterPriority;
    private RecyclerView rvTasks;
    private FloatingActionButton fabAddTask;

    private DatabaseHelper databaseHelper;
    private TaskAdapter taskAdapter;
    private List<Task> currentTaskList = new ArrayList<>();

    private ActivityResultLauncher<Intent> addEditTaskLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupRecyclerView();
        setupFilterSpinners();
        setupSearchListener();
        setupActivityResultLauncher();

        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
            addEditTaskLauncher.launch(intent);
        });

        loadTasksAndStats();
    }

    private void initViews() {
        tvStatTotal = findViewById(R.id.tvStatTotal);
        tvStatCompleted = findViewById(R.id.tvStatCompleted);
        tvStatPending = findViewById(R.id.tvStatPending);
        tvEmpty = findViewById(R.id.tvEmpty);
        etSearch = findViewById(R.id.etSearch);
        spFilterStatus = findViewById(R.id.spFilterStatus);
        spFilterPriority = findViewById(R.id.spFilterPriority);
        rvTasks = findViewById(R.id.rvTasks);
        fabAddTask = findViewById(R.id.fabAddTask);
    }

    private void setupRecyclerView() {
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(this, currentTaskList, this);
        rvTasks.setAdapter(taskAdapter);
    }

    private void setupFilterSpinners() {
        String[] statusOptions = new String[]{"Tất cả", "Chưa thực hiện", "Đã hoàn thành"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterStatus.setAdapter(statusAdapter);

        String[] priorityOptions = new String[]{"Tất cả", "Thấp", "Trung bình", "Cao"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorityOptions);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterPriority.setAdapter(priorityAdapter);

        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadTasksAndStats();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        spFilterStatus.setOnItemSelectedListener(spinnerListener);
        spFilterPriority.setOnItemSelectedListener(spinnerListener);
    }

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadTasksAndStats();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupActivityResultLauncher() {
        addEditTaskLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadTasksAndStats();
                    }
                }
        );
    }

    private void loadTasksAndStats() {
        String keyword = etSearch.getText().toString().trim();
        String statusFilter = spFilterStatus.getSelectedItem() != null ? spFilterStatus.getSelectedItem().toString() : "Tất cả";
        String priorityFilter = spFilterPriority.getSelectedItem() != null ? spFilterPriority.getSelectedItem().toString() : "Tất cả";

        // Get filtered tasks for RecyclerView
        currentTaskList = databaseHelper.getFilteredTasks(keyword, statusFilter, priorityFilter);
        taskAdapter.updateList(currentTaskList);

        if (currentTaskList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }

        // Calculate global statistics (from all tasks in database)
        List<Task> allTasks = databaseHelper.getAllTasks();
        int totalCount = allTasks.size();
        int completedCount = 0;
        for (Task t : allTasks) {
            if (t.isCompleted()) {
                completedCount++;
            }
        }
        int pendingCount = totalCount - completedCount;

        tvStatTotal.setText(getString(R.string.stats_total, totalCount));
        tvStatCompleted.setText(getString(R.string.stats_completed, completedCount));
        tvStatPending.setText(getString(R.string.stats_pending, pendingCount));
    }

    @Override
    public void onEdit(Task task) {
        Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskActivity.EXTRA_TASK, task);
        addEditTaskLauncher.launch(intent);
    }

    @Override
    public void onDelete(Task task) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_msg)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    boolean success = databaseHelper.deleteTask(task.getId());
                    if (success) {
                        Toast.makeText(MainActivity.this, "Xóa công việc thành công!", Toast.LENGTH_SHORT).show();
                        loadTasksAndStats();
                    } else {
                        Toast.makeText(MainActivity.this, "Xóa công việc thất bại!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public void onStatusChange(Task task, boolean isCompleted) {
        boolean success = databaseHelper.updateTaskStatus(task.getId(), isCompleted);
        if (success) {
            loadTasksAndStats();
        } else {
            Toast.makeText(this, "Cập nhật trạng thái thất bại!", Toast.LENGTH_SHORT).show();
        }
    }
}
