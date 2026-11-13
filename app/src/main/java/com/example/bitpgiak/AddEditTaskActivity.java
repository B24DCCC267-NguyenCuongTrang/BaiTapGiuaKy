package com.example.bitpgiak;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class AddEditTaskActivity extends AppCompatActivity {

    public static final String EXTRA_TASK = "extra_task";

    private TextView tvFormTitle, tvDueDate;
    private EditText etTaskTitle, etTaskDescription;
    private Button btnSaveTask, btnCancelTask;
    private RadioGroup rgPriority;
    private RadioButton rbPriorityLow, rbPriorityMedium, rbPriorityHigh;

    private DatabaseHelper databaseHelper;
    private Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        checkIntentData();
        setupListeners();
    }

    private void initViews() {
        tvFormTitle = findViewById(R.id.tvFormTitle);
        tvDueDate = findViewById(R.id.tvDueDate);
        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        btnSaveTask = findViewById(R.id.btnSaveTask);
        btnCancelTask = findViewById(R.id.btnCancelTask);
        rgPriority = findViewById(R.id.rgPriority);
        rbPriorityLow = findViewById(R.id.rbPriorityLow);
        rbPriorityMedium = findViewById(R.id.rbPriorityMedium);
        rbPriorityHigh = findViewById(R.id.rbPriorityHigh);
    }

    private void checkIntentData() {
        if (getIntent().hasExtra(EXTRA_TASK)) {
            currentTask = (Task) getIntent().getSerializableExtra(EXTRA_TASK);
        }

        if (currentTask != null) {
            tvFormTitle.setText(R.string.edit_task);
            btnSaveTask.setText("Cập nhật");

            etTaskTitle.setText(currentTask.getTitle());
            etTaskDescription.setText(currentTask.getDescription());

            String dueDate = currentTask.getDueDate();
            if (!TextUtils.isEmpty(dueDate)) {
                tvDueDate.setText(dueDate);
            }

            String priority = currentTask.getPriority();
            if ("Cao".equalsIgnoreCase(priority)) {
                rbPriorityHigh.setChecked(true);
            } else if ("Trung bình".equalsIgnoreCase(priority)) {
                rbPriorityMedium.setChecked(true);
            } else {
                rbPriorityLow.setChecked(true);
            }
        } else {
            tvFormTitle.setText(R.string.add_task);
            btnSaveTask.setText(R.string.btn_save);

            // Default due date to today
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            String todayStr = String.format(Locale.getDefault(), "%02d/%02d/%d", day, month + 1, year);
            tvDueDate.setText(todayStr);
            rbPriorityLow.setChecked(true);
        }
    }

    private void setupListeners() {
        if (tvDueDate != null) {
            tvDueDate.setOnClickListener(v -> {
                Toast.makeText(this, "Mở lịch chọn ngày...", Toast.LENGTH_SHORT).show();
                showDatePicker();
            });
        }

        btnSaveTask.setOnClickListener(v -> saveTask());
        btnCancelTask.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        String currentDueDateStr = tvDueDate.getText().toString().trim();
        if (!TextUtils.isEmpty(currentDueDateStr)) {
            try {
                String[] parts = currentDueDateStr.split("/");
                if (parts.length == 3) {
                    int day = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]) - 1;
                    int year = Integer.parseInt(parts[2]);
                    calendar.set(year, month, day);
                }
            } catch (Exception ignored) {
            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    tvDueDate.setText(formattedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void saveTask() {
        String title = etTaskTitle.getText().toString().trim();
        String description = etTaskDescription.getText().toString().trim();
        String dueDate = tvDueDate.getText().toString().trim();

        String priority = "Thấp";
        int checkedId = rgPriority.getCheckedRadioButtonId();
        if (checkedId == R.id.rbPriorityHigh) {
            priority = "Cao";
        } else if (checkedId == R.id.rbPriorityMedium) {
            priority = "Trung bình";
        }

        if (TextUtils.isEmpty(title)) {
            etTaskTitle.setError(getString(R.string.empty_title_error));
            Toast.makeText(this, getString(R.string.empty_title_error), Toast.LENGTH_SHORT).show();
            etTaskTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(dueDate)) {
            dueDate = "";
        }

        if (currentTask == null) {
            // Thêm mới
            Task newTask = new Task(title, description, dueDate, priority, false);
            long result = databaseHelper.insertTask(newTask);
            if (result > 0) {
                Toast.makeText(this, "Thêm công việc thành công!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Thêm công việc thất bại!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Cập nhật
            currentTask.setTitle(title);
            currentTask.setDescription(description);
            currentTask.setDueDate(dueDate);
            currentTask.setPriority(priority);

            boolean success = databaseHelper.updateTask(currentTask);
            if (success) {
                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
