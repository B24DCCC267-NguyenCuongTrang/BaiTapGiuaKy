package com.example.bitpgiak;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TaskManager.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_TASKS = "tasks";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DUE_DATE = "due_date";
    public static final String COLUMN_PRIORITY = "priority";
    public static final String COLUMN_IS_COMPLETED = "is_completed";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_TASKS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_DUE_DATE + " TEXT, " +
                COLUMN_PRIORITY + " TEXT, " +
                COLUMN_IS_COMPLETED + " INTEGER DEFAULT 0" +
                ")";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    public long insertTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_DESCRIPTION, task.getDescription());
        values.put(COLUMN_DUE_DATE, task.getDueDate());
        values.put(COLUMN_PRIORITY, task.getPriority());
        values.put(COLUMN_IS_COMPLETED, task.isCompleted() ? 1 : 0);

        long id = db.insert(TABLE_TASKS, null, values);
        db.close();
        return id;
    }

    public boolean updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_DESCRIPTION, task.getDescription());
        values.put(COLUMN_DUE_DATE, task.getDueDate());
        values.put(COLUMN_PRIORITY, task.getPriority());
        values.put(COLUMN_IS_COMPLETED, task.isCompleted() ? 1 : 0);

        int rowsAffected = db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
        return rowsAffected > 0;
    }

    public boolean updateTaskStatus(int taskId, boolean isCompleted) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_COMPLETED, isCompleted ? 1 : 0);

        int rowsAffected = db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_TASKS, COLUMN_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
        return rowsAffected > 0;
    }

    public List<Task> getAllTasks() {
        return getFilteredTasks("", "Tất cả", "Tất cả");
    }

    public List<Task> getFilteredTasks(String keyword, String statusFilter, String priorityFilter) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder selectionBuilder = new StringBuilder("1=1");
        List<String> selectionArgsList = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            selectionBuilder.append(" AND ").append(COLUMN_TITLE).append(" LIKE ?");
            selectionArgsList.add("%" + keyword.trim() + "%");
        }

        if (statusFilter != null) {
            if (statusFilter.equalsIgnoreCase("Đã hoàn thành")) {
                selectionBuilder.append(" AND ").append(COLUMN_IS_COMPLETED).append(" = 1");
            } else if (statusFilter.equalsIgnoreCase("Chưa thực hiện")) {
                selectionBuilder.append(" AND ").append(COLUMN_IS_COMPLETED).append(" = 0");
            }
        }

        if (priorityFilter != null && !priorityFilter.equalsIgnoreCase("Tất cả")) {
            selectionBuilder.append(" AND ").append(COLUMN_PRIORITY).append(" = ?");
            selectionArgsList.add(priorityFilter);
        }

        String selection = selectionBuilder.toString();
        String[] selectionArgs = selectionArgsList.toArray(new String[0]);

        Cursor cursor = db.query(
                TABLE_TASKS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                COLUMN_ID + " DESC"
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                    String dueDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE));
                    String priority = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY));
                    boolean isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1;

                    Task task = new Task(id, title, description, dueDate, priority, isCompleted);
                    taskList.add(task);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();

        return taskList;
    }
}
