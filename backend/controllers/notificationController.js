const asyncHandler = require("express-async-handler")
const Notification = require("../models/notificationModel")

// @desc    Get all notifications for a user
// @route   GET /api/notifications
// @access  Private
const getNotifications = asyncHandler(async (req, res) => {
    const notifications = await Notification.find({ user: req.user._id })
        .sort({ createdAt: -1 })
        .limit(50)
    res.json(notifications)
})

// @desc    Get unread notification count
// @route   GET /api/notifications/unread-count
// @access  Private
const getUnreadCount = asyncHandler(async (req, res) => {
    const count = await Notification.countDocuments({ user: req.user._id, isRead: false })
    res.json({ count })
})

// @desc    Mark notification as read
// @route   PUT /api/notifications/:id
// @access  Private
const markAsRead = asyncHandler(async (req, res) => {
    const notification = await Notification.findById(req.params.id)

    if (notification) {
        if (notification.user.toString() !== req.user._id.toString()) {
            res.status(401)
            throw new Error("Not authorized")
        }
        notification.isRead = true
        await notification.save()
        res.json(notification)
    } else {
        res.status(404)
        throw new Error("Notification not found")
    }
})

// @desc    Mark all notifications as read
// @route   PUT /api/notifications/read-all
// @access  Private
const markAllAsRead = asyncHandler(async (req, res) => {
    await Notification.updateMany(
        { user: req.user._id, isRead: false },
        { $set: { isRead: true } }
    )
    res.json({ message: "All notifications marked as read" })
})

// @desc    Delete a notification
// @route   DELETE /api/notifications/:id
// @access  Private
const deleteNotification = asyncHandler(async (req, res) => {
    const notification = await Notification.findById(req.params.id)

    if (notification) {
        if (notification.user.toString() !== req.user._id.toString()) {
            res.status(401)
            throw new Error("Not authorized")
        }
        await notification.deleteOne()
        res.json({ message: "Notification removed" })
    } else {
        res.status(404)
        throw new Error("Notification not found")
    }
})

// @desc    Clear all notifications for a user
// @route   DELETE /api/notifications
// @access  Private
const clearAllNotifications = asyncHandler(async (req, res) => {
    await Notification.deleteMany({ user: req.user._id })
    res.json({ message: "All notifications cleared" })
})

// @desc    Create a notification
// @route   POST /api/notifications
// @access  Private
const createNotification = asyncHandler(async (req, res) => {
    const { userId, type, title, message, relatedId } = req.body

    const notification = await Notification.create({
        user: userId || req.user._id,
        type,
        title,
        message,
        relatedId,
        isRead: false
    })

    res.status(201).json(notification)
})

// Helper: create notification internally (no HTTP needed)
const createNotificationInternal = async (userId, type, title, message, relatedId = null) => {
    try {
        await Notification.create({
            user: userId,
            type,
            title,
            message,
            relatedId,
            isRead: false
        })
    } catch (err) {
        console.error("Failed to create internal notification:", err.message)
    }
}

module.exports = {
    getNotifications,
    getUnreadCount,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    clearAllNotifications,
    createNotification,
    createNotificationInternal
}
