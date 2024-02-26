package aura.wozart.com.aura.entity.amazonaws.mobile.util

/***************************************************************************
 * File Name :
 * Author : Aarth Tandel
 * Date of Creation : 30/04/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/

object StringFormatUtils {

    /**
     * Converts number of bytes into proper scale.
     *
     * @param bytes number of bytes to be converted.
     * @param higherPrecision flag to show two more digits of precision after the decimal.
     * @return A string that represents the bytes in a proper scale.
     */
    fun getBytesString(bytes: Long, higherPrecision: Boolean): String {
        val quantifiers = arrayOf("KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var i = 0
        while (true) {
            if (i >= quantifiers.size) {
                return "\u221E"
            }
            size /= 1024.0
            if (size < 512) {
                return if (higherPrecision) {
                    String.format("%.2f %s", size, quantifiers[i])
                } else {
                    String.format("%d %s", Math.round(size), quantifiers[i])
                }
            }
            i++
        }
    }
}
/** This utility class is not constructable.  */