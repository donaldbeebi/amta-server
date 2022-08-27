package com.donald.musictheoryapp.util.practiceoptions

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONArray
import org.json.JSONObject

class SectionOption(
    val number: Int,
    val identifier: String,
    val name: String,
    val options: List<QuestionGroupOption>
) : Iterable<QuestionGroupOption>, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createTypedArrayList(QuestionGroupOption)!!
    )

    override fun iterator() = options.iterator()

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(number)
        parcel.writeString(identifier)
        parcel.writeString(name)
        parcel.writeTypedList(options)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SectionOption> {
        override fun createFromParcel(parcel: Parcel): SectionOption {
            return SectionOption(parcel)
        }

        override fun newArray(size: Int): Array<SectionOption?> {
            return arrayOfNulls(size)
        }
    }

    fun toJson() = JSONObject().apply {
        put("identifier", identifier)
        put(
            "groups",
            JSONArray().apply { options.forEach { put(it.toJson()) } }
        )
    }
}

typealias PracticeOptions = List<SectionOption>

fun PracticeOptions.toJson() = JSONObject().apply {
    put(
        "sections",
        JSONArray().apply {
            forEach { sectionOption -> put(sectionOption.toJson()) }
        }
    )
}

fun PracticeOptions.countPoints() = sumOf { sectionOption ->
    sectionOption.options.sumOf { it.count }
}