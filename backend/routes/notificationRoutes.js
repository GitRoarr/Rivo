const express = require("express")
const router = express.Router()
const {
    getNotifications,
    getUnreadCount,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    clearAllNotifications,
    createNotification
} = require("../controllers/notificationController")
const { protect } = require("../middleware/authMiddleware")

// Specific routes first (to avoid conflict with /:id)
router.get("/unread-count", protect, getUnreadCount)
router.put("/read-all", protect, markAllAsRead)

router.route("/")
    .get(protect, getNotifications)
    .post(protect, createNotification)
    .delete(protect, clearAllNotifications)

router.route("/:id")
    .put(protect, markAsRead)
    .delete(protect, deleteNotification)

module.exports = router
