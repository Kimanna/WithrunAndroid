package com.example.withrun
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable
import org.json.JSONObject

class RoomIntoUser () : Parcelable{

    private var id: Int? = null
    private var nickname: String? = null
    private var profileImgUrl: String? = null
    private var distanceGap: Int? = null
    private var roomManager: Boolean? = false
    private var invited: Boolean? = false
    private var avgPace: Int? = null
    private var fcmToken: String? = null

    private var runningState: Int? = null // 러닝중인지 체크하는 변수 ==> 입장 전 = 0 / 러닝중 = 1 / 중도포기 = 2
    private var myActiveRaceRank: Int? = null // 현재 순위 저장
    private var fixUserRanking: Int? = null // 확정된 랭킹

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        nickname = parcel.readString()
        profileImgUrl = parcel.readString()
        distanceGap = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    fun getId(): Int? {
        return id
    }

    fun setId(id: Int?) {
        this.id = id
    }

    fun getNickname(): String? {
        return nickname
    }

    fun setNickname(nickname: String) {
        this.nickname = nickname
    }
    fun getProfileImgUrl(): String? {
        return profileImgUrl
    }

    fun setProfileImgUrl(profileImgUrl: String?) {
        this.profileImgUrl = profileImgUrl
    }

    fun getDistanceGap(): Int? {
        return distanceGap
    }

    fun setDistanceGap(distanceGap: Int?) {
        this.distanceGap = distanceGap
    }

    fun getRoomManager(): Boolean? {
        return roomManager
    }

    fun setRoomManager(roomManager: Boolean?) {
        this.roomManager = roomManager
    }

    fun getInvited(): Boolean? {
        return invited
    }

    fun setInvited(invited: Boolean?) {
        this.invited = invited
    }

    fun getAvgPace(): Int? {
        return avgPace
    }

    fun setAvgPace(avgPace: Int?) {
        this.avgPace = avgPace
    }

    fun getFcmToken(): String? {
        return fcmToken
    }

    fun setFcmToken(fcmToken: String) {
        this.fcmToken = fcmToken
    }

    fun getRunningState(): Int? {
        return runningState
    }

    fun setRunningState(runningState: Int?) {
        this.runningState = runningState
    }

    fun getMyActiveRaceRank(): Int? {
        return myActiveRaceRank
    }

    fun setMyActiveRaceRank(myActiveRaceRank: Int?) {
        this.myActiveRaceRank = myActiveRaceRank
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(nickname)
        parcel.writeString(profileImgUrl)
        parcel.writeValue(distanceGap)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RoomIntoUser> {
        override fun createFromParcel(parcel: Parcel): RoomIntoUser {
            return RoomIntoUser(parcel)
        }

        override fun newArray(size: Int): Array<RoomIntoUser?> {
            return arrayOfNulls(size)
        }
    }
}