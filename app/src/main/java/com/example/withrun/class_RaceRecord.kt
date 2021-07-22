package com.example.withrun

class class_RaceRecord {

    private var id: Int? = null
    private var roomNo: Int? = null
    private var intoAt: String? = null
    private var gameStartTime: String? = null
    private var gameFinishTime: String? = null
    private var raceTime: Int? = null
    private var distance: Int? = null
    private var raceAvgPace: Int? = null
    private var raceAvgSpeed: Int? = null

    private var raceAvgPaceString: String? = null
    private var raceAvgSpeedString: String? = null

    private var myRanking: Int? = null
    private var completed: Int? = null
    private var deleteRoom: Int? = null
    private var activeRunState: Int? = null
    private var mapSnapshot: String? = null

    private var recordNickname: String? = null
    private var profileImgPath: String? = null

    private var runStartTime: String? = null

    private var gameFinishTimeToLong: Long? = null

    private var memberCount: Int? = null

    private var isLoading: Int? = null

    private var monthlyString: String? = null
    private var recordMonthCount: Int? = null
    private var totalMonthDistance: Int? = null


    fun getId(): Int? {
        return id
    }

    fun setId(id: Int?) {
        this.id = id
    }

    fun getRoomNo(): Int? {
        return roomNo
    }

    fun setRoomNo(roomNo: Int?) {
        this.roomNo = roomNo
    }

    fun getIntoAt(): String? {
        return intoAt
    }

    fun setIntoAt(intoAt: String?) {
        this.intoAt = intoAt
    }

    fun getGameStartTime(): String? {
        return gameStartTime
    }

    fun setGameStartTime(gameStartTime: String?) {
        this.gameStartTime = gameStartTime
    }

    fun getGameFinishTime(): String? {
        return gameFinishTime
    }

    fun setGameFinishTime(gameFinishTime: String?) {
        this.gameFinishTime = gameFinishTime
    }

    fun getRaceTime(): Int? {
        return raceTime
    }

    fun setRaceTime(raceTime: Int?) {
        this.raceTime = raceTime
    }

    fun getDistance(): Int? {
        return distance
    }

    fun setDistance(distance: Int?) {
        this.distance = distance
    }

    fun getRaceAvgPace(): Int? {
        return raceAvgPace
    }

    fun setRaceAvgPace(raceAvgPace: Int?) {
        this.raceAvgPace = raceAvgPace
    }

    fun getRaceAvgSpeed(): Int? {
        return raceAvgSpeed
    }

    fun setRaceAvgSpeed(raceAvgSpeed: Int?) {
        this.raceAvgSpeed = raceAvgSpeed
    }

    fun getRaceAvgPaceString(): String? {
        return raceAvgPaceString
    }

    fun setRaceAvgPaceString(raceAvgPaceString: String?) {
        this.raceAvgPaceString = raceAvgPaceString
    }

    fun getRaceAvgSpeedString(): String? {
        return raceAvgSpeedString
    }

    fun setRaceAvgSpeedString(raceAvgSpeedString: String?) {
        this.raceAvgSpeedString = raceAvgSpeedString
    }

    fun getMyRanking(): Int? {
        return myRanking
    }

    fun setMyRanking(myRanking: Int?) {
        this.myRanking = myRanking
    }

    fun getCompleted(): Int? {
        return completed
    }

    fun setCompleted(completed: Int?) {
        this.completed = completed
    }

    fun getDeleteRoom(): Int? {
        return deleteRoom
    }

    fun setDeleteRoom(deleteRoom: Int?) {
        this.deleteRoom = deleteRoom
    }

    fun getActiveRunState(): Int? {
        return activeRunState
    }

    fun setActiveRunState(activeRunState: Int?) {
        this.activeRunState = activeRunState
    }

    fun getMapSnapshot(): String? {
        return mapSnapshot
    }

    fun setMapSnapshot(mapSnapshot: String) {
        this.mapSnapshot = mapSnapshot
    }

    fun getRecordNickname(): String? {
        return recordNickname
    }

    fun setRecordNickname(recordNickname: String) {
        this.recordNickname = recordNickname
    }

    fun getProfileImgPath(): String? {
        return profileImgPath
    }

    fun setProfileImgPath(profileImgPath: String) {
        this.profileImgPath = profileImgPath
    }

    fun getRunStartTime(): String? {
        return runStartTime
    }

    fun setRunStartTime(runStartTime: String) {
        this.runStartTime = runStartTime
    }


    fun getGameFinishTimeToLong(): Long? {
        return gameFinishTimeToLong
    }

    fun setGameFinishTimeToLong(gameFinishTimeToLong: Long?) {
        this.gameFinishTimeToLong = gameFinishTimeToLong
    }

    fun getMemberCount(): Int? {
        return memberCount
    }

    fun setMemberCount (memberCount: Int?) {
        this.memberCount = memberCount
    }

    fun getIsLoading(): Int? {
        return isLoading
    }

    fun setIsLoading(isLoading: Int?) {
        this.isLoading = isLoading
    }

    fun getMonthlyString(): String? {
        return monthlyString
    }

    fun setMonthlyString(monthlyString: String) {
        this.monthlyString = monthlyString
    }

    fun getRecordMonthCount(): Int? {
        return recordMonthCount
    }

    fun setRecordMonthCount(recordMonthCount: Int?) {
        this.recordMonthCount = recordMonthCount
    }
    fun getTotalMonthDistance(): Int? {
        return totalMonthDistance
    }

    fun setTotalMonthDistance(totalMonthDistance: Int?) {
        this.totalMonthDistance = totalMonthDistance
    }


}