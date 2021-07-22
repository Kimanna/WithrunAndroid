package com.example.withrun

import android.os.Parcel
import android.os.Parcelable

class  Follow_object() : Parcelable {

    private var meId: Int? = null
    private var meNickname: String? = null
    private var meImg: String? = null
    private var youId: Int? = null
    private var youNickname: String? = null
    private var youImg: String? = null
    private var acceptStatus: Int? = null
    private var requestAt: String? = null
    private var acceptAt: String? = null
    private var hideNewsfeed: Int? = null
    private var hideFollower: Int? = null
    private var recordOpenStatus: Int? = null
    private var avgPaceGap: Int? = null

    private var followLayoutNo: Int = 1

    private var uniqueNo: Int? = null

    private var fcmToken: String? = null
    private var avgPace: String? = null

    constructor(parcel: Parcel) : this() {
        meId = parcel.readValue(Int::class.java.classLoader) as? Int
        meNickname = parcel.readString()
        meImg = parcel.readString()
        youId = parcel.readValue(Int::class.java.classLoader) as? Int
        youNickname = parcel.readString()
        youImg = parcel.readString()
        acceptStatus = parcel.readValue(Int::class.java.classLoader) as? Int
        requestAt = parcel.readString()
        acceptAt = parcel.readString()
        hideNewsfeed = parcel.readValue(Int::class.java.classLoader) as? Int
        hideFollower = parcel.readValue(Int::class.java.classLoader) as? Int
        recordOpenStatus = parcel.readInt()
        avgPaceGap = parcel.readInt()
        followLayoutNo = parcel.readInt()
        uniqueNo = parcel.readValue(Int::class.java.classLoader) as? Int
        fcmToken = parcel.readString()
        avgPace = parcel.readString()

    }


    fun getFollowLayoutNo(): Int {
        return followLayoutNo
    }

    fun getMeId(): Int? {
        return meId
    }

    fun setMeId(meId: Int?) {
        this.meId = meId
    }

    fun getMeNickname(): String? {
        return meNickname
    }

    fun setMeNickname(meNickname: String?) {
        this.meNickname = meNickname
    }

    fun getMeImg(): String? {
        return meImg
    }

    fun setMeImg(meImg: String?) {
        this.meImg = meImg
    }

    fun getYouId(): Int? {
        return youId
    }

    fun setYouId(youId: Int?) {
        this.youId = youId
    }

    fun getYouNickname(): String? {
        return youNickname
    }

    fun setYouNickname(youNickname: String?) {
        this.youNickname = youNickname
    }

    fun getYouImg(): String? {
        return youImg
    }

    fun setYouImg(youImg: String?) {
        this.youImg = youImg
    }

    fun getAcceptStatus(): Int? {
        return acceptStatus
    }

    fun setAcceptStatus(acceptStatus: Int?) {
        this.acceptStatus = acceptStatus
    }

    fun getRequestAt(): String? {
        return requestAt
    }

    fun setRequestAt(requestAt: String?) {
        this.requestAt = requestAt
    }

    fun getAcceptAt(): String? {
        return acceptAt
    }

    fun setAcceptAt(acceptAt: String?) {
        this.acceptAt = acceptAt
    }

    fun getHideNewsfeed(): Int? {
        return hideNewsfeed
    }

    fun setHideNewsfeed(hideNewsfeed: Int?) {
        this.hideNewsfeed = hideNewsfeed
    }

    fun getHideFollower(): Int? {
        return hideFollower
    }

    fun setHideFollower(hideFollower: Int?) {
        this.hideFollower = hideFollower
    }

    fun getRecordOpenStatus(): Int? {
        return recordOpenStatus
    }

    fun setRecordOpenStatus(recordOpenStatus: Int?) {
        this.recordOpenStatus = recordOpenStatus
    }
    fun getAvgPaceGap(): Int? {
        return avgPaceGap
    }

    fun setAvgPaceGap(avgPaceGap: Int?) {
        this.avgPaceGap = avgPaceGap
    }

    fun getUniqueNo(): Int? {
        return uniqueNo
    }

    fun setUniqueNo(uniqueNo: Int?) {
        this.uniqueNo = uniqueNo
    }

    fun getFcmToken(): String? {
        return acceptAt
    }

    fun setFcmToken(fcmToken: String?) {
        this.fcmToken = fcmToken
    }

    fun getAvgPace(): String? {
        return avgPace
    }

    fun setAvgPace(avgPace: String?) {
        this.avgPace = avgPace
    }



    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(meId)
        parcel.writeString(meNickname)
        parcel.writeString(meImg)
        parcel.writeValue(youId)
        parcel.writeString(youNickname)
        parcel.writeString(youImg)
        parcel.writeValue(acceptStatus)
        parcel.writeString(requestAt)
        parcel.writeString(acceptAt)
        parcel.writeValue(hideNewsfeed)
        parcel.writeValue(hideFollower)
        parcel.writeValue(recordOpenStatus)
        parcel.writeValue(avgPaceGap)
        parcel.writeInt(followLayoutNo)
        parcel.writeValue(uniqueNo)
        parcel.writeString(fcmToken)
        parcel.writeString(avgPace)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Follow_object> {
        override fun createFromParcel(parcel: Parcel): Follow_object {
            return Follow_object(parcel)
        }

        override fun newArray(size: Int): Array<Follow_object?> {
            return arrayOfNulls(size)
        }
    }


}