package com.example.withrun

class RoomItem () {

    private var no: Int? = null
    private var createdAt: String? = null
    private var roomImgPath: String? = null
    private var startDate: String? = null
    private var roomTitle: String? = null
    private var startTime: String? = null
    private var finishTime: String? = null
    private var distance: Int? = null
    private var sortGender: String? = null
    private var sortLevel: String? = null
    private var roomManager: Int? = null
    private var maxPeople: Int? = null
    private var memberCount: Int? = null
    private var aloneMode: Int? = null
    private var activeGame: Int? = null
    private var isLoading: Int? = null


    fun getNo(): Int? {
        return no
    }

    fun setNo(no: Int?) {
        this.no = no
    }

    fun getCreatedAt(): String? {
        return createdAt
    }

    fun setCreatedAt(createdAt: String?) {
        this.createdAt = createdAt
    }


    fun getRoomImgPath(): String? {
        return roomImgPath
    }

    fun setRoomImgPath(roomImgPath: String?) {
        this.roomImgPath = roomImgPath
    }

    fun getStartDate(): String? {
        return startDate
    }

    fun setStartDate(startDate: String?) {
        this.startDate = startDate
    }

    fun getRoomTitle(): String? {
        return roomTitle
    }

    fun setRoomTitle(roomTitle: String?) {
        this.roomTitle = roomTitle
    }

    fun getStartTime(): String? {
        return startTime
    }

    fun setStartTime(startTime: String?) {
        this.startTime = startTime
    }

    fun getFinishTime(): String? {
        return finishTime
    }

    fun setFinishTime(finishTime: String?) {
        this.finishTime = finishTime
    }

    fun getDistance(): Int? {
        return distance
    }

    fun setDistance(distance: Int?) {
        this.distance = distance
    }

    fun getSortGender(): String? {
        return sortGender
    }

    fun setSortGender(sortGender: String?) {
        this.sortGender = sortGender
    }

    fun getSortLevel(): String? {
        return sortLevel
    }

    fun setSortLevel(sortLevel: String?) {
        this.sortLevel = sortLevel
    }

    fun getRoomManager(): Int? {
        return roomManager
    }

    fun setRoomManager(roomManager: Int?) {
        this.roomManager = roomManager
    }

    fun getMaxPeople(): Int? {
        return maxPeople
    }

    fun setMaxPeople(maxPeople: Int?) {
        this.maxPeople = maxPeople
    }

    fun getMemberCount(): Int? {
        return memberCount
    }

    fun setMemberCount(memberCount: Int?) {
        this.memberCount = memberCount
    }
    fun getAloneMode(): Int? {
        return aloneMode
    }

    fun setAloneMode(aloneMode: Int?) {
        this.aloneMode = aloneMode
    }

    fun getActiveGame(): Int? {
        return activeGame
    }

    fun setActiveGame(activeGame: Int?) {
        this.activeGame = activeGame
    }

    fun getIsLoading(): Int? {
        return isLoading
    }

    fun setIsLoading(isLoading: Int?) {
        this.isLoading = isLoading
    }

}