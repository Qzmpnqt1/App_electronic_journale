package com.example.electronic_journal.server.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Subject(
    @SerializedName("subjectId")
    var subjectId: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("course")
    var course: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(subjectId)
        parcel.writeString(name)
        parcel.writeInt(course)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Subject> {
        override fun createFromParcel(parcel: Parcel): Subject {
            return Subject(parcel)
        }

        override fun newArray(size: Int): Array<Subject?> {
            return arrayOfNulls(size)
        }
    }
}
