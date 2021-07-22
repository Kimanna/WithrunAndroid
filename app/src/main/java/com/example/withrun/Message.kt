package com.example.withrun

class Message {

    private var id: Int? = null
    private var nickname: String? = null
    private var profileImgUrl: String? = null

    private var messagetype : Int = 0 // 메시지 보낸사람 0, 받은사람 1, 구분선 2
    private var messageText:String? = null
    private var messageDate: Long? = null


    private var Key_UserID  : String? = null // from

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

    fun getMessagetype(): Int {
        return messagetype
    }

    fun setMessagetype(messagetype: Int) {
        this.messagetype = messagetype
    }

    fun getKey_UserID(): String? {
        return Key_UserID
    }

    fun setKey_UserID(key_UserID: String) {
        this.Key_UserID = key_UserID
    }

    fun getMessageText(): String? {
        return messageText
    }

    fun setMessageText(messageText: String?) {
        this.messageText = messageText
    }

    fun getMessageDate(): Long? {
        return messageDate
    }

    fun setMessageDate(messageDate: Long) {
        this.messageDate = messageDate
    }


}