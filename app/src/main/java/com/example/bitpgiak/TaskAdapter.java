package com.example.bitpgiak;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskActionListener {
        void onEdit(Task task);
        void onDelete(Task task);
        void onStatusChange(Task task, boolean isCompleted);
    }

    private final Context context;
    private List<Task> taskList;
    private final OnTaskActionListener listener;

    public TaskAdapter(Context context, List<Task> taskList, OnTaskActionListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    public void updateList(List<Task> newList) {
        this.taskList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.tvTaskIdAndTitle.setText(String.format(Locale.getDefault(), "#%d - %s", task.getId(), task.getTitle()));
        holder.tvTaskDescription.setText(task.getDescription() != null && !task.getDescription().isEmpty() 
                ? task.getDescription() : "Không có mô tả");
        
        String dueDateStr = task.getDueDate() != null && !task.getDueDate().isEmpty() 
                ? task.getDueDate() : "Không có";
        holder.tvTaskDueDate.setText("Hạn: " + dueDateStr);

        // Priority
        String priority = task.getPriority() != null ? task.getPriority() : "Thấp";
        holder.tvTaskPriority.setText(priority);

        if ("Cao".equalsIgnoreCase(priority)) {
            holder.tvTaskPriority.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_high));
        } else if ("Trung bình".equalsIgnoreCase(priority)) {
            holder.tvTaskPriority.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_medium));
        } else {
            holder.tvTaskPriority.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_low));
        }

        // Status & Strike-through effect
        holder.cbTaskCompleted.setOnCheckedChangeListener(null);
        holder.cbTaskCompleted.setChecked(task.isCompleted());

        if (task.isCompleted()) {
            holder.tvTaskStatus.setText("Đã hoàn thành");
            holder.tvTaskStatus.setTextColor(ContextCompat.getColor(context, R.color.status_completed));
            holder.tvTaskIdAndTitle.setPaintFlags(holder.tvTaskIdAndTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTaskStatus.setText("Chưa thực hiện");
            holder.tvTaskStatus.setTextColor(ContextCompat.getColor(context, R.color.status_pending));
            holder.tvTaskIdAndTitle.setPaintFlags(holder.tvTaskIdAndTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Listeners
        holder.cbTaskCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onStatusChange(task, isChecked);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(task);
            }
        });

        holder.btnEditTask.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(task);
            }
        });

        holder.btnDeleteTask.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbTaskCompleted;
        TextView tvTaskIdAndTitle, tvTaskDueDate, tvTaskPriority, tvTaskDescription, tvTaskStatus;
        Button btnEditTask, btnDeleteTask;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbTaskCompleted = itemView.findViewById(R.id.cbTaskCompleted);
            tvTaskIdAndTitle = itemView.findViewById(R.id.tvTaskIdAndTitle);
            tvTaskDueDate = itemView.findViewById(R.id.tvTaskDueDate);
            tvTaskPriority = itemView.findViewById(R.id.tvTaskPriority);
            tvTaskDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvTaskStatus = itemView.findViewById(R.id.tvTaskStatus);
            btnEditTask = itemView.findViewById(R.id.btnEditTask);
            btnDeleteTask = itemView.findViewById(R.id.btnDeleteTask);
        }
    }
}
