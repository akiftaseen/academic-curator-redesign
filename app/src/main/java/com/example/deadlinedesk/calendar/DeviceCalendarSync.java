package com.example.deadlinedesk.calendar;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import androidx.core.content.ContextCompat;

import com.example.deadlinedesk.data.Deadline;

import java.util.TimeZone;

public final class DeviceCalendarSync {

    public static final long NO_EVENT_ID = -1L;
    private static final long ONE_HOUR_MILLIS = 60L * 60L * 1000L;

    private DeviceCalendarSync() {
        // Utility class.
    }

    public static long upsertDeadlineEvent(Context context, Deadline deadline) {
        if (context == null || deadline == null) {
            return NO_EVENT_ID;
        }

        if (!hasCalendarPermissions(context)) {
            return deadline.getCalendarEventId();
        }

        Long calendarId = findPrimaryWritableCalendarId(context);
        if (calendarId == null) {
            return deadline.getCalendarEventId();
        }

        ContentResolver resolver = context.getContentResolver();
        ContentValues eventValues = buildEventValues(deadline, calendarId);
        long existingEventId = deadline.getCalendarEventId();

        if (existingEventId > 0L) {
            Uri existingEventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, existingEventId);
            int rows = resolver.update(existingEventUri, eventValues, null, null);
            if (rows > 0) {
                syncReminder(resolver, existingEventId, deadline.getReminderMinutes());
                return existingEventId;
            }
        }

        Uri inserted = resolver.insert(CalendarContract.Events.CONTENT_URI, eventValues);
        if (inserted == null) {
            return deadline.getCalendarEventId();
        }

        long insertedEventId = ContentUris.parseId(inserted);
        syncReminder(resolver, insertedEventId, deadline.getReminderMinutes());
        return insertedEventId;
    }

    public static void deleteDeadlineEvent(Context context, long eventId) {
        if (context == null || eventId <= 0L || !hasCalendarPermissions(context)) {
            return;
        }

        Uri eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
        context.getContentResolver().delete(eventUri, null, null);
    }

    private static boolean hasCalendarPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED;
    }

    private static ContentValues buildEventValues(Deadline deadline, long calendarId) {
        long startTime = deadline.getDueDate();
        long endTime = startTime + ONE_HOUR_MILLIS;

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        values.put(CalendarContract.Events.TITLE, deadline.getTitle());
        values.put(CalendarContract.Events.DESCRIPTION, buildDescription(deadline));
        values.put(CalendarContract.Events.EVENT_LOCATION, emptyToBlank(deadline.getModule()));
        values.put(CalendarContract.Events.DTSTART, startTime);
        values.put(CalendarContract.Events.DTEND, endTime);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        values.put(CalendarContract.Events.HAS_ALARM, 1);
        return values;
    }

    private static String buildDescription(Deadline deadline) {
        String notes = emptyToBlank(deadline.getNotes());
        String priority = emptyToBlank(deadline.getPriority());

        if (priority.isEmpty()) {
            return notes;
        }

        if (notes.isEmpty()) {
            return "Priority: " + priority;
        }

        return notes + "\n\nPriority: " + priority;
    }

    private static String emptyToBlank(String value) {
        return value == null ? "" : value;
    }

    private static Long findPrimaryWritableCalendarId(Context context) {
        String[] projection = {
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.IS_PRIMARY,
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                CalendarContract.Calendars.VISIBLE,
                CalendarContract.Calendars.SYNC_EVENTS
        };

        String selection = CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + ">=?"
                + " AND " + CalendarContract.Calendars.VISIBLE + "=1"
                + " AND " + CalendarContract.Calendars.SYNC_EVENTS + "=1";
        String[] selectionArgs = {
                String.valueOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR)
        };

        String sortOrder = CalendarContract.Calendars.IS_PRIMARY + " DESC, "
                + CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + " DESC, "
                + CalendarContract.Calendars._ID + " ASC";

        try (Cursor cursor = context.getContentResolver().query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        }

        return null;
    }

    private static void syncReminder(ContentResolver resolver, long eventId, int reminderMinutes) {
        if (eventId <= 0L) {
            return;
        }

        resolver.delete(CalendarContract.Reminders.CONTENT_URI,
                CalendarContract.Reminders.EVENT_ID + "=?",
                new String[]{String.valueOf(eventId)});

        if (reminderMinutes <= 0) {
            return;
        }

        ContentValues reminderValues = new ContentValues();
        reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId);
        reminderValues.put(CalendarContract.Reminders.MINUTES, reminderMinutes);
        reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        resolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);
    }
}
