package com.example.withrun
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable
import org.json.JSONObject

class Room_object () : Parcelable {

    private var id: Int? = null
    private var nickname: String? = null
    private var profileImgUrl: String? = null
    private var roomManager: Boolean? = false
    private var invited: Boolean? = false


    private var no: Int? = null
    private var startDateTime: String? = null

    private var uniqueNo: Int? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        nickname = parcel.readString()
        profileImgUrl = parcel.readString()
        roomManager = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        invited = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        no = parcel.readValue(Int::class.java.classLoader) as? Int
        startDateTime = parcel.readString()
        uniqueNo = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    fun getNo(): Int? {
        return no
    }

    fun setNo(no: Int?) {
        this.no = no
    }

    fun getStartDateTime(): String? {
        return startDateTime
    }

    fun setStartDateTime(startDateTime: String?) {
        this.startDateTime = startDateTime
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

    fun getUniqueNo(): Int? {
        return uniqueNo
    }

    fun setUniqueNo(uniqueNo: Int?) {
        this.uniqueNo = uniqueNo
    }

    companion object CREATOR : Parcelable.Creator<Room_object> {
        override fun createFromParcel(parcel: Parcel): Room_object {
            return Room_object(parcel)
        }

        override fun newArray(size: Int): Array<Room_object?> {
            return arrayOfNulls(size)
        }
    }

    override fun writeToParcel(parcel: Parcel, p1: Int) {
        parcel.writeValue(id)
        parcel.writeString(nickname)
        parcel.writeString(profileImgUrl)
        parcel.writeValue(roomManager)
        parcel.writeValue(invited)
        parcel.writeValue(no)
        parcel.writeString(startDateTime)
        parcel.writeValue(uniqueNo)
    }

    override fun describeContents(): Int {
        return 0
    }

}