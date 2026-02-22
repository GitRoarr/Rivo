const express = require("express")
const router = express.Router()
const {
    getFeaturedContent,
    addFeaturedContent,
    updateFeaturedContent,
    deleteFeaturedContent
} = require("../controllers/featuredContentController")
const { protect, admin } = require("../middleware/authMiddleware")

router.route("/")
    .get(getFeaturedContent)
    .post(protect, admin, addFeaturedContent)

router.route("/:id")
    .put(protect, admin, updateFeaturedContent)
    .delete(protect, admin, deleteFeaturedContent)

module.exports = router
