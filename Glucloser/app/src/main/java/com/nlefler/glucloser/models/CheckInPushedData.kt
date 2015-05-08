package com.nlefler.glucloser.models

/**
 * Created by Nathan Lefler on 12/29/14.
 */
public class CheckInPushedData {
    private val checkInData: CheckInData? = null

    class CheckInData {
        internal val venueId: String? = null
        internal val venueName: String? = null
        internal val venueLat: String? = null
        internal val venueLon: String? = null
    }

    public fun getVenueId(): String {
        return checkInData?.venueId ?: ""
    }

    public fun getVenueName(): String {
        return checkInData?.venueName ?: ""
    }

    public fun getVenueLat(): Float {
        if (checkInData?.venueLat?.length() ?: 0 > 0) {
            return java.lang.Float.valueOf(checkInData!!.venueLat)
        } else {
            return 0f
        }
    }

    public fun getVenueLon(): Float {
        if (checkInData?.venueLon?.length() ?: 0 > 0) {
            return java.lang.Float.valueOf(checkInData!!.venueLon)
        } else {
            return 0f
        }
    }

}
