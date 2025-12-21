package ecarx.xsf.mediacenter

import android.os.Binder
import android.os.IBinder
import android.os.IInterface

interface IMusicPlaybackInfo : IInterface {
    abstract class Stub : Binder(), IMusicPlaybackInfo {
        override fun asBinder(): IBinder = this

        companion object {
            const val DESCRIPTOR = "ecarx.xsf.mediacenter.IMusicPlaybackInfo"

            fun asInterface(binder: IBinder?): IMusicPlaybackInfo? {
                if (binder == null) return null
                val iin = binder.queryLocalInterface(DESCRIPTOR)
                return if (iin != null && iin is IMusicPlaybackInfo) {
                    iin
                } else {
                    object : IMusicPlaybackInfo {
                        override fun asBinder(): IBinder = binder
                    }
                }
            }
        }
    }
}
