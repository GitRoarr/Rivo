const asyncHandler = require("express-async-handler")
const FeaturedContent = require("../models/featuredContentModel")
const { v4: uuidv4 } = require('uuid')

// @desc    Get all featured content
// @route   GET /api/featured
// @access  Public
const getFeaturedContent = asyncHandler(async (req, res) => {
    const featured = await FeaturedContent.find({ isActive: true }).sort({ order: 1 })
    res.json(featured)
})

// @desc    Add featured content
// @route   POST /api/featured
// @access  Private (Admin)
const addFeaturedContent = asyncHandler(async (req, res) => {
    const { type, contentId, title, description, imageUrl, actionUrl, order } = req.body

    const featured = await FeaturedContent.create({
        id: uuidv4(),
        type,
        contentId,
        title,
        description,
        imageUrl,
        actionUrl,
        order: order || 0
    })

    res.status(201).json(featured)
})

// @desc    Update featured content
// @route   PUT /api/featured/:id
// @access  Private (Admin)
const updateFeaturedContent = asyncHandler(async (req, res) => {
    const featured = await FeaturedContent.findOne({ id: req.params.id })

    if (featured) {
        featured.type = req.body.type || featured.type
        featured.contentId = req.body.contentId !== undefined ? req.body.contentId : featured.contentId
        featured.title = req.body.title || featured.title
        featured.description = req.body.description !== undefined ? req.body.description : featured.description
        featured.imageUrl = req.body.imageUrl || featured.imageUrl
        featured.actionUrl = req.body.actionUrl || featured.actionUrl
        featured.order = req.body.order !== undefined ? req.body.order : featured.order
        featured.isActive = req.body.isActive !== undefined ? req.body.isActive : featured.isActive

        const updatedFeatured = await featured.save()
        res.json(updatedFeatured)
    } else {
        res.status(404)
        throw new Error("Featured content not found")
    }
})

// @desc    Delete featured content
// @route   DELETE /api/featured/:id
// @access  Private (Admin)
const deleteFeaturedContent = asyncHandler(async (req, res) => {
    const featured = await FeaturedContent.findOne({ id: req.params.id })

    if (featured) {
        await featured.remove()
        res.json({ message: "Featured content removed" })
    } else {
        res.status(404)
        throw new Error("Featured content not found")
    }
})

module.exports = {
    getFeaturedContent,
    addFeaturedContent,
    updateFeaturedContent,
    deleteFeaturedContent
}
