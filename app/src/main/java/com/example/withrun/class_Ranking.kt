package com.example.withrun

class class_Ranking {

    private var id: Int? = null

    private var nickname: String? = null
    private var profileImgPath: String? = null

    private var goldMedal: Int? = null
    private var silverMedal: Int? = null
    private var bronzeMedal: Int? = null

    fun getId(): Int? {
        return id
    }

    fun setId(id: Int?) {
        this.id = id
    }

    fun getNickname(): String? {
        return nickname
    }

    fun setNickname(nickname: String?) {
        this.nickname = nickname
    }

    fun getProfileImgPath(): String? {
        return profileImgPath
    }

    fun setProfileImgPath(profileImgPath: String?) {
        this.profileImgPath = profileImgPath
    }

    fun getGoldMedal(): Int? {
        return goldMedal
    }

    fun setGoldMedal(goldMedal: Int?) {
        this.goldMedal = goldMedal
    }

    fun getSilverMedal(): Int? {
        return silverMedal
    }

    fun setSilverMedal(silverMedal: Int?) {
        this.silverMedal = silverMedal
    }

    fun getBronzeMedal(): Int? {
        return bronzeMedal
    }

    fun setBronzeMedal(bronzeMedal: Int?) {
        this.bronzeMedal = bronzeMedal
    }

}