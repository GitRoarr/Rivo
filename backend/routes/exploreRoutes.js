const express = require("express")
const router = express.Router()
const { getExploreData } = require("../controllers/exploreController")

router.get("/", getExploreData)

module.exports = router
