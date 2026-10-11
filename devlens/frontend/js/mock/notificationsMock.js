/**
 * DevLens Mock Notifications Module
 */

let initialNotifications = [
  {
    id: 1,
    title: "Analysis Completed",
    message: "BubbleSort.java achieved quality score 88/100 (Grade A).",
    timeAgo: "10m ago",
    read: false,
    createdAt: "2026-10-11T12:30:00Z"
  },
  {
    id: 2,
    title: "Project Health Alert",
    message: "CS201 Algorithms indexed successfully with 4 files.",
    timeAgo: "2h ago",
    read: false,
    createdAt: "2026-10-11T10:00:00Z"
  },
  {
    id: 3,
    title: "Midterm Audit Available",
    message: "Code review report for CS201 Midterm is ready to view.",
    timeAgo: "1d ago",
    read: true,
    createdAt: "2026-10-10T15:00:00Z"
  }
];

export function mockGetNotifications() {
  return [...initialNotifications];
}

export function mockMarkNotificationRead(id) {
  const item = initialNotifications.find(n => n.id === Number(id));
  if (item) {
    item.read = true;
  }
  return { success: true, id };
}

export function mockMarkAllNotificationsRead() {
  initialNotifications.forEach(n => { n.read = true; });
  return { success: true };
}
