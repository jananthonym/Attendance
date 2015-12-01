package com.mobileapp.attendance.helper;

/**
 * Created by Jan on 11/26/15.
 */
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mobileapp.attendance.model.*;
import com.mobileapp.attendance.model.Class;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = DatabaseHelper.class.getName();

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "contactsManager";

    // Table Names
    private static final String TABLE_STUDENTS = "students";
    private static final String TABLE_CLASSES = "classes";
    private static final String TABLE_STUDENT_CLASS = "student_class";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";

    // NOTES Table - column names
    private static final String KEY_SNAME= "sname";
    private static final String KEY_MAC = "mac";

    // TAGS Table - column names
    private static final String KEY_CNAME = "cname";

    // NOTE_TAGS Table - column names
    private static final String KEY_STUDENT_ID = "student_id";
    private static final String KEY_CLASS_ID = "class_id";

    // Student table create statement
    private static final String CREATE_TABLE_STUDENT = "CREATE TABLE "
            + TABLE_STUDENTS + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_SNAME
            + " TEXT," + KEY_MAC + " TEXT," + KEY_CREATED_AT
            + " DATETIME" + ")";

    // CLASS table create statement
    private static final String CREATE_TABLE_CLASS = "CREATE TABLE " + TABLE_CLASSES
            + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_CNAME + " TEXT,"
            + KEY_CREATED_AT + " DATETIME" + ")";

    // student_class table create statement
    private static final String CREATE_TABLE_STUDENT_CLASS = "CREATE TABLE "
            + TABLE_STUDENT_CLASS + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_STUDENT_ID + " INTEGER," + KEY_CLASS_ID + " INTEGER,"
            + KEY_CREATED_AT + " DATETIME" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_STUDENT);
        db.execSQL(CREATE_TABLE_CLASS);
        db.execSQL(CREATE_TABLE_STUDENT_CLASS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENT_CLASS);

        // create new tables
        onCreate(db);
    }

    // ------------------------ "todos" table methods ----------------//

    /**
     * Creating a todo
     */
    public long createStudent(Student student, long class_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SNAME, student.getName());
        values.put(KEY_MAC, student.getMac());
        values.put(KEY_CREATED_AT, getDateTime());

        // insert row
        long todo_id = db.insert(TABLE_STUDENTS, null, values);

        // insert tag_ids
        //for (long tag_id : tag_ids) {
            createStudentClass(todo_id, class_id);
        //}

        return todo_id;
    }

    /**
     * get single student
     */
    public Student getStudent(long student_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_STUDENTS + " WHERE "
                + KEY_ID + " = " + student_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Student td = new Student();
        td.setId(c.getInt(c.getColumnIndex(KEY_ID)));
        td.setName((c.getString(c.getColumnIndex(KEY_SNAME))));
        td.setMac((c.getString(c.getColumnIndex(KEY_MAC))));

        return td;
    }

    /**
     * getting all students
     * */
    public List<Student> getAllStudents() {
        List<Student> todos = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_STUDENTS;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Student td = new Student();
                td.setId(c.getInt((c.getColumnIndex(KEY_ID))));
                td.setName((c.getString(c.getColumnIndex(KEY_SNAME))));
                td.setMac((c.getString(c.getColumnIndex(KEY_MAC))));

                // adding to todo list
                todos.add(td);
            } while (c.moveToNext());
        }

        return todos;
    }

    /**
     * getting all todos under single tag
     * */
    public List<Student> getAllStudentsByClass(String name) {
        List<Student> todos = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_STUDENTS + " td, "
                + TABLE_CLASSES + " tg, " + TABLE_STUDENT_CLASS + " tt WHERE tg."
                + KEY_CNAME + " = '" + name + "'" + " AND tg." + KEY_ID
                + " = " + "tt." + KEY_CLASS_ID + " AND td." + KEY_ID + " = "
                + "tt." + KEY_STUDENT_ID;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Student td = new Student();
                td.setId(c.getInt((c.getColumnIndex(KEY_ID))));
                td.setName((c.getString(c.getColumnIndex(KEY_SNAME))));
                td.setMac((c.getString(c.getColumnIndex(KEY_MAC))));

                // adding to todo list
                todos.add(td);
            } while (c.moveToNext());
        }

        return todos;
    }

    /**
     * getting todo count
     */
    public int getStudentCount() {
        String countQuery = "SELECT  * FROM " + TABLE_STUDENTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    /**
     * Updating a todo
     */
    public int updateStudent(Student student) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SNAME, student.getName());
        values.put(KEY_MAC, student.getMac());

        // updating row
        return db.update(TABLE_STUDENTS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(student.getId()) });
    }

    /**
     * Deleting a todo
     */
    public void deleteStudent(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STUDENTS, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    // ------------------------ "class" table methods ----------------//

    /**
     * Creating tag
     */
    public long createClass(Class clas) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CNAME, clas.getName());
        values.put(KEY_CREATED_AT, getDateTime());

        // insert row
        long tag_id = db.insert(TABLE_CLASSES, null, values);

        return tag_id;
    }

    /**
     * getting all tags
     * */
    public List<Class> getAllClasses() {
        List<Class> tags = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_CLASSES;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Class t = new Class();
                t.setId(c.getInt((c.getColumnIndex(KEY_ID))));
                t.setName(c.getString(c.getColumnIndex(KEY_CNAME)));

                // adding to tags list
                tags.add(t);
            } while (c.moveToNext());
        }
        return tags;
    }

    /**
     * Updating a tag
     */
    public int updateClass(Class tag) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CNAME, tag.getName());

        // updating row
        return db.update(TABLE_CLASSES, values, KEY_ID + " = ?",
                new String[] { String.valueOf(tag.getId()) });
    }

    /**
     * Deleting a tag
     */
    public void deleteClass(Class tag, boolean should_delete_all_tag_todos) {
        SQLiteDatabase db = this.getWritableDatabase();

        // before deleting tag
        // check if todos under this tag should also be deleted
        if (should_delete_all_tag_todos) {
            // get all todos under this tag
            List<Student> allTagToDos = getAllStudentsByClass(tag.getName());

            // delete all todos
            for (Student todo : allTagToDos) {
                // delete todo
                deleteStudent(todo.getId());
            }
        }

        // now delete the tag
        db.delete(TABLE_CLASSES, KEY_ID + " = ?",
                new String[] { String.valueOf(tag.getId()) });
    }

    // ------------------------ "todo_tags" table methods ----------------//

    /**
     * Creating todo_tag
     */
    public long createStudentClass(long stu_id, long cla_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_STUDENT_ID, stu_id);
        values.put(KEY_CLASS_ID, cla_id);
        values.put(KEY_CREATED_AT, getDateTime());

        long id = db.insert(TABLE_STUDENT_CLASS, null, values);

        return id;
    }

    /**
     * Updating a todo tag
     */
    public int updateStudentClass(long id, long tag_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CLASS_ID, tag_id);

        // updating row
        return db.update(TABLE_STUDENT_CLASS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    /**
     * Deleting a todo tag
     */
    public void deleteStudentClass(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STUDENT_CLASS, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    /**
     * get datetime
     * */
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
