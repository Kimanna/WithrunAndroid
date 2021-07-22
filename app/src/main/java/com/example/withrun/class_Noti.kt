package com.example.withrun

class class_Noti {

    private var id: Int? = null
    private var receivedAt: String? = null
    private var message: String? = null
    private var readStatus: Int? = null
    private var deleteNoti: Int? = null
    private var notiType: String? = null
    private var uniqueNo: Int? = null

    fun getId(): Int? {
        return id
    }

    fun setId(id: Int?) {
        this.id = id
    }

    fun getReceivedAt(): String? {
        return receivedAt
    }

    fun setReceivedAt(receivedAt: String?) {
        this.receivedAt = receivedAt
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String?) {
        this.message = message
    }

    fun getReadStatus(): Int? {
        return readStatus
    }

    fun setReadStatus(readStatus: Int?) {
        this.readStatus = readStatus
    }

    fun getDeleteNoti(): Int? {
        return deleteNoti
    }

    fun setDeleteNoti(deleteNoti: Int?) {
        this.deleteNoti = deleteNoti
    }

    fun getNotiType(): String? {
        return notiType
    }

    fun setNotiType(notiType: String?) {
        this.notiType = notiType
    }

    fun getUniqueNo(): Int? {
        return uniqueNo
    }

    fun setUniqueNo(uniqueNo: Int?) {
        this.uniqueNo = uniqueNo
    }

}