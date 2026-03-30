# DeadlineDesk - Project Proposal

## 1. Project Overview
We’re planning to build a small native Android app (primarily in Java) to help students stay on top of assignment deadlines. The idea is fairly straightforward: users add assignments/tests with a due date (and maybe a couple of simple details like module and priority), the app shows everything in an upcoming list alongside a basic calendar view, and it can send reminder notifications so things don’t slip. It’s not meant to be complicated or “feature-heavy” — more something that would actually be useful day to day for keeping track of what’s coming up and when.
Implementation-wise, we’ll keep the structure clean by separating the UI layer (Activities/Fragments) from the data and storage side through a single repository-style class (e.g., DeadlineRepository). Deadlines will be stored locally.

## 2. Feature List
| Priority | Feature                     | Difficulty   | Description                                                                                                                                   |
| -------- | --------------------------- | ------------ | --------------------------------------------------------------------------------------------------------------------------------------------- |
| Core 1   | Deadline Manager            | Medium       | Add/edit/delete deadlines: title, module, due date/time, priority, notes, done. Upcoming list sorted by time + module filter.                 |
| Core 2   | Calendar View + Agenda      | Medium       | Month grid view. Tap a day, RecyclerView agenda for that date shows. Days with deadlines are highlighted.                                     |
| Core 3   | Reminders + Notifications   | Medium–High  | Reminder offsets: 1 hr / 1 day / 1 week before . Notification actions: Mark Done + Snooze.                                                    |
| Stretch  | Export to Device Calendar   | Medium       | CalendarContract write. 'Add to Calendar' button inserts event via a ContentResolver.                                                         |

We will prioritise functional completeness and reliability over advanced UI styling. Hopefully all features can be implemented but exporting to a devices calendar will be dropped if we do not have time.

## 3. Android Component Mapping
| Component Type    | Class                   | Role                                                                                              |
| ----------------- | ----------------------- | ------------------------------------------------------------------------------------------------- |
| Activity          | MainActivity            | Hosts bottom navigation + fragment container for main app flow.                                   |
| Activity          | AddEditDeadlineActivity | Create/edit form screen; launched via explicit Intent with deadline ID extra.                     |
| Fragment          | CalendarFragment        | Custom month grid; manages selected-date state.                                                   |
| Fragment          | AgendaFragment          | RecyclerView list of deadlines for the tapped day.                                                |
| Fragment          | DeadlineDetailFragment  | Full deadline details; Edit / Delete / Remind / Export actions.                                   |
| BroadcastReceiver | ReminderReceiver        | Receives alarm intent → posts notification + handles Done/Snooze action intents.                  |
| ContentProvider   | CalendarContract        | ContentResolver insert to add deadline as device calendar event (stretch).                        |
| Persistence       | Room DAO / SQLiteOpenHelper | Local deadline storage behind DeadlineRepository interface.                                       |

## 4. Architecture
• UI Layer: Activities + Fragments. Display + interaction only; no business logic.
• Repository: DeadlineRepository single API: insert, update, delete, getAll, getByDate.
• Persistence: SQLiteOpenHelper. Swappable; no upstream changes needed.
• Reminders: ReminderScheduler wraps AlarmManager/WorkManager

## 5. Division of workload
**Person A — UI & Navigation**
- CalendarFragment (month grid & onClick individual day)
- AgendaFragment (RecyclerView & adapter)
- DeadlineDetailFragment
- AddEditDeadlineActivity (form & validation)
- MainActivity (nav & Intent wiring)
- All XML layouts

**Person B — Data, Reminders & Integration**
- Deadline model + DeadlineRepository
- ReminderScheduler
- ReminderReceiver + BootReceiver
- Notification channels + action intents
- CalendarContract export (if we get to it)

Source control: We will use a github repository and keep a timeline of our project via frequent commits & proper branching with merges upon completion of a task.
No firm decision on who is Person A & B, this will be decided after we get feedback and start the implementation of the project.

## 6. Low-Fidelity Wireframes
These may very well change during the implementation as they are only to give us an initial idea
- **Wireframe A — Calendar**: Month grid — days shown in 7-col layout. Days with deadlines highlighted. Tap day → AgendaFragment updates below grid. Agenda rows: title + time
- **Wireframe B — Add / Edit Deadline Form**: Fields: Title, Module, Date/Time picker. Priority dropdown. Notes multi-line text area. Reminder offset spinner + Save / Cancel buttons
- **Wireframe C — Deadline Detail Screen**: Shows all deadline fields read-only. Action bar: Edit · Delete · Mark Done. Reminder section: current offset + change option. 'Add to Calendar' button.
- **Wireframe D — Reminder Notification**: System notification: title + module + time remaining. Two action buttons: ‘Mark Done’ & Snooze. Snooze pushes another notification after 15 minutes.

## 7. Project Phases
1. **Setup**: project scaffold, Deadline model, DeadlineRepository interface, Room/SQLite schema, navigation skeleton.
2. **Core**: AddEditDeadlineActivity, DeadlineDetailFragment, upcoming list sorted and filtered by module.
3. **Calendar + Agenda**: CalendarFragment month grid, day-tap → AgendaFragment RecyclerView, mark-done from list.
4. **Reminders**: ReminderScheduler, AlarmManager scheduling, ReminderReceiver, notification actions (Done / Snooze), BootReceiver.
5. **Stretch**: CalendarContract export, runtime permissions, UI refinements, edge-case testing.
6. **Testing**: end-to-end device testing, bug fixes

## 8. Technologies we plan to use
- Java + Android SDK: Module requirement; full Android component support.
- XML layouts + Fragments + RecyclerView: Standard Android UI; matches teaching material.
- AlarmManager: AlarmManager for precise timing.
- NotificationChannel + BroadcastReceiver actions: API 26+ standard channel; ReminderReceiver handles Done/Snooze intents.
- CalendarContract ContentProvider: For the exporting to android calendar if we decide to implent