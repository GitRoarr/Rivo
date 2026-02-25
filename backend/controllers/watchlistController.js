const asyncHandler = require("express-async-handler")
const Watchlist = require("../models/watchlistModel")


const createWatchlist = asyncHandler(async (req, res) => {
    const { id, name, description } = req.body

    if (!id || !name) {
        res.status(400)
        throw new Error("Please provide watchlist ID and name")
    }

    const watchlistExists = await Watchlist.findOne({ id })

    if (watchlistExists) {
        res.status(400)
        throw new Error("Watchlist with this ID already exists")
    }

    const watchlist = await Watchlist.create({
        id,
        name,
        description: description || "",
        createdBy: req.user.id,
        songs: [],
    })

    if (watchlist) {
        res.status(201).json(watchlist)
    } else {
        res.status(400)
        throw new Error("Invalid watchlist data")
    }
})

const getUserWatchlists = asyncHandler(async (req, res) => {
    const watchlists = await Watchlist.find({ createdBy: req.user.id })
    res.json(watchlists)
})


const getWatchlistById = asyncHandler(async (req, res) => {
    const watchlist = await Watchlist.findOne({ id: req.params.id }).populate("songs")

    if (!watchlist) {
        res.status(404)
        throw new Error("Watchlist not found")
    }

    if (watchlist.createdBy !== req.user.id && req.user.userType !== "ADMIN") {
        res.status(403)
        throw new Error("Not authorized to view this watchlist")
    }

    res.json(watchlist)
})

const updateWatchlist = asyncHandler(async (req, res) => {
    const watchlist = await Watchlist.findOne({ id: req.params.id })

    if (!watchlist) {
        res.status(404)
        throw new Error("Watchlist not found")
    }

    if (watchlist.createdBy !== req.user.id && req.user.userType !== "ADMIN") {
        res.status(403)
        throw new Error("Not authorized to update this watchlist")
    }

    watchlist.name = req.body.name || watchlist.name
    watchlist.description = req.body.description !== undefined ? req.body.description : watchlist.description

    const updatedWatchlist = await watchlist.save()
    res.json(updatedWatchlist)
})


const deleteWatchlist = asyncHandler(async (req, res) => {
    const watchlist = await Watchlist.findOne({ id: req.params.id })

    if (!watchlist) {
        res.status(404)
        throw new Error("Watchlist not found")
    }

    if (watchlist.createdBy !== req.user.id && req.user.userType !== "ADMIN") {
        res.status(403)
        throw new Error("Not authorized to delete this watchlist")
    }

    await watchlist.remove()
    res.json({ message: "Watchlist removed" })
})


const addSongToWatchlist = asyncHandler(async (req, res) => {
    const { musicId } = req.body

    if (!musicId) {
        res.status(400)
        throw new Error("Please provide a music ID")
    }

    const watchlist = await Watchlist.findOne({ id: req.params.id })

    if (!watchlist) {
        res.status(404)
        throw new Error("Watchlist not found")
    }

    if (watchlist.createdBy !== req.user.id && req.user.userType !== "ADMIN") {
        res.status(403)
        throw new Error("Not authorized to update this watchlist")
    }

    if (watchlist.songs.includes(musicId)) {
        res.status(400)
        throw new Error("Song already in watchlist")
    }

    watchlist.songs.push(musicId)
    const updatedWatchlist = await watchlist.save()

    res.json(updatedWatchlist)
})


const removeSongFromWatchlist = asyncHandler(async (req, res) => {
    const watchlist = await Watchlist.findOne({ id: req.params.id })

    if (!watchlist) {
        res.status(404)
        throw new Error("Watchlist not found")
    }

    if (watchlist.createdBy !== req.user.id && req.user.userType !== "ADMIN") {
        res.status(403)
        throw new Error("Not authorized to update this watchlist")
    }

    const songIndex = watchlist.songs.indexOf(req.params.musicId)
    if (songIndex === -1) {
        res.status(400)
        throw new Error("Song not in watchlist")
    }

    watchlist.songs.splice(songIndex, 1)
    const updatedWatchlist = await watchlist.save()

    res.json(updatedWatchlist)
})

const checkSongInWatchlists = asyncHandler(async (req, res) => {
    const watchlists = await Watchlist.find({
        createdBy: req.user.id,
        songs: req.params.musicId,
    })

    const response = {
        inWatchlist: watchlists.length > 0,
        watchlists: watchlists.map((watchlist) => ({
            id: watchlist.id,
            name: watchlist.name,
        })),
    }

    res.json(response)
})

module.exports = {
    createWatchlist,
    getUserWatchlists,
    getWatchlistById,
    updateWatchlist,
    deleteWatchlist,
    addSongToWatchlist,
    removeSongFromWatchlist,
    checkSongInWatchlists,
}