@file:Suppress("DEPRECATION")

package com.wozart.aura.aura.utilities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Environment
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import com.wozart.aura.R
import com.wozart.aura.data.model.tutorial.TutorialModel
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.Constant.Companion.AC
import com.wozart.aura.utilities.Constant.Companion.BED_LAMP
import com.wozart.aura.utilities.Constant.Companion.BULB
import com.wozart.aura.utilities.Constant.Companion.CURTAIN
import com.wozart.aura.utilities.Constant.Companion.EXAUSTFAN
import com.wozart.aura.utilities.Constant.Companion.FAN
import com.wozart.aura.utilities.Constant.Companion.LAMP
import com.wozart.aura.utilities.Constant.Companion.PLUG
import com.wozart.aura.utilities.Constant.Companion.SOCKET
import com.wozart.aura.utilities.DialogListener
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Saif on 3/16/2018.
 */

object Utils {

    fun getImage(imageName: String?, context: Context): Int {
        return context.resources.getIdentifier(imageName, "drawable", context.packageName)
    }

    fun showCustomDialog(context: Context, title: String, message: String, layout: Int, listener: DialogListener) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(layout)
        val no = dialog.findViewById<View>(R.id.btn_no) as Button
        val yes = dialog.findViewById<View>(R.id.btn_yes) as Button
        val titleTV = dialog.findViewById<View>(R.id.tv_title) as TextView
        val messageTV = dialog.findViewById<View>(R.id.message) as TextView
        titleTV.text = title
        messageTV.text = message

        no.setOnClickListener {
            listener.onCancelClicked()
            dialog.dismiss()
        }
        yes.setOnClickListener {
            listener.onOkClicked()
            dialog.dismiss()
        }
        dialog.show()
    }

    fun showCustomDialogOk(context: Context, title: String, message: String, layout: Int, listener: DialogListener) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(layout)
        val no = dialog.findViewById<View>(R.id.btn_no) as Button
        val yes = dialog.findViewById<View>(R.id.btn_yes) as Button
        val titleTV = dialog.findViewById<View>(R.id.tv_title) as TextView
        val messageTV = dialog.findViewById<View>(R.id.message) as TextView
        titleTV.text = title
        messageTV.text = message
        no.setOnClickListener {
            listener.onCancelClicked()
            dialog.dismiss()
        }
        yes.setOnClickListener {
            listener.onOkClicked()
            dialog.dismiss()
        }
        dialog.show()
    }

    fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(
                Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (activity.currentFocus != null)
            inputMethodManager.hideSoftInputFromWindow(
                    activity.currentFocus!!.windowToken, InputMethodManager.SHOW_FORCED)
    }

    public fun getScreenWidthInDP1(context: Context): Int {
        Resources.getSystem().getDisplayMetrics().widthPixels;
        val dm = DisplayMetrics()
        val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)
        val widthInDP = Math.round(dm.widthPixels / dm.density)
        return widthInDP.toInt() * 2
    }

    @SuppressLint("ObsoleteSdkInt")
    fun getScreenWidthInDP(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val outPoint: Point = Point()
        if (Build.VERSION.SDK_INT >= 19) {
            display.getRealSize(outPoint)
        } else {
            display.getSize(outPoint)
        }
        if (outPoint.y > outPoint.x) {
            return outPoint.x
        } else {
            return outPoint.y
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    fun setDrawable(context: Context, view: ViewGroup) {
        val originalBitmap: Bitmap?
        val f = File(getWallpaperPath(context))
        if (f.exists()) {
            val bitmapSizeOptions = BitmapFactory.Options()
            bitmapSizeOptions.inJustDecodeBounds = true
            bitmapSizeOptions.inDither = false
            bitmapSizeOptions.inPurgeable = true
            bitmapSizeOptions.inInputShareable = true
            originalBitmap = BitmapFactory.decodeFile(f.absolutePath, bitmapSizeOptions)
            //originalBitmap = BitmapFactory.decodeFile(f.absolutePath)
        } else
            originalBitmap = BitmapFactory.decodeResource(context.resources,
                    getWallpaperDrawables(getWallpaperId(context)))
        // val blurredBitmap = BlurBuilder.blur(context, originalBitmap!!)
        view.background = BitmapDrawable(context.resources, originalBitmap)
    }

    @SuppressLint("ObsoleteSdkInt")
    fun setDrawable(context: Context, view: ViewGroup, home: String) {
        val originalBitmap: Bitmap?
        val f = File(getWallpaperPath(context))
        if (f.exists()) {
            val bitmapSizeOptions = BitmapFactory.Options()
            bitmapSizeOptions.inJustDecodeBounds = true
            bitmapSizeOptions.inDither = false
            bitmapSizeOptions.inPurgeable = true
            bitmapSizeOptions.inInputShareable = true
            originalBitmap = BitmapFactory.decodeFile(f.absolutePath, bitmapSizeOptions)
            //originalBitmap = BitmapFactory.decodeFile(f.absolutePath)
        } else
            originalBitmap = BitmapFactory.decodeResource(context.resources,
                    getWallpaperDrawables(getWallpaperId(context, home)))
        // val blurredBitmap = BlurBuilder.blur(context, originalBitmap!!)
        view.background = BitmapDrawable(context.resources, originalBitmap)
    }

    fun setRoomDrawable(context: Context, view: ViewGroup, wallpaperId: Int) {
        val originalBitmap: Bitmap?
        val f = File(getWallpaperPath(context))
        if (f.exists()) {
            val bitmapSizeOptions = BitmapFactory.Options()
            bitmapSizeOptions.inJustDecodeBounds = true
            bitmapSizeOptions.inDither = false
            bitmapSizeOptions.inPurgeable = true
            bitmapSizeOptions.inInputShareable = true
            originalBitmap = BitmapFactory.decodeFile(f.absolutePath, bitmapSizeOptions)
            //originalBitmap = BitmapFactory.decodeFile(f.absolutePath)
        } else
            originalBitmap = BitmapFactory.decodeResource(context.resources,
                    getRoomWallpaperDrawables(wallpaperId))
        // val blurredBitmap = BlurBuilder.blur(context, originalBitmap!!)
        view.background = BitmapDrawable(context.resources, originalBitmap)
    }

    @SuppressLint("ObsoleteSdkInt")
    fun setDrawable(context: Context, view: ViewGroup, wallpaperId: Int) {
        val originalBitmap: Bitmap?
        val f = File(getWallpaperPath(context))
        if (f.exists()) {
            val bitmapSizeOptions = BitmapFactory.Options()
            bitmapSizeOptions.inJustDecodeBounds = true
            bitmapSizeOptions.inDither = false
            bitmapSizeOptions.inPurgeable = true
            bitmapSizeOptions.inInputShareable = true
            originalBitmap = BitmapFactory.decodeFile(f.absolutePath, bitmapSizeOptions)
            //originalBitmap = BitmapFactory.decodeFile(f.absolutePath)
        } else
            originalBitmap = BitmapFactory.decodeResource(context.resources,
                    getWallpaperDrawables(wallpaperId))
        // val blurredBitmap = BlurBuilder.blur(context, originalBitmap!!)
        view.background = BitmapDrawable(context.resources, originalBitmap)
    }

    @SuppressLint("ApplySharedPref")
    fun saveWallPaperId(context: Context, value: Int) {
        val sharedPref = context.getSharedPreferences("wozart", Context.MODE_PRIVATE)
        sharedPref.edit().putInt(context.getString(R.string.saved_wallpaper_key), value).commit()
        //89655
        saveThemeType(context)
    }

    @SuppressLint("ApplySharedPref")
    fun saveWallPaperId(context: Context, value: Int, home: String) {
        val sharedPref = context.getSharedPreferences("wozart", Context.MODE_PRIVATE)
        sharedPref.edit().putInt(home, value).commit()
        //89655
        saveThemeType(context)
    }

    @SuppressLint("ApplySharedPref")
    fun saveWallPaperPath(context: Context, value: String) {
        val sharedPref = context.getSharedPreferences("wozart", Context.MODE_PRIVATE)
        sharedPref.edit().putString(context.getString(R.string.saved_wallpaper_path_key), value).commit()
        saveThemeType(context)
    }

    @SuppressLint("ApplySharedPref")
    private fun saveThemeId(context: Context, value: Int) {
        val sharedPref = context.getSharedPreferences("wozart", Context.MODE_PRIVATE)
        sharedPref.edit().putInt(context.getString(R.string.saved_theme_key), value).commit()
    }

    fun getWallpaperId(context: Context): Int {
        val sharedPref = context.getSharedPreferences("wozart", Context.MODE_PRIVATE)
        val wallpaperId: Int = sharedPref.getInt(context.getString(R.string.saved_wallpaper_key), 0)
        if (wallpaperId > 12)
            return 0
        else return wallpaperId
    }

    fun getWallpaperId(context: Context, home: String): Int {
        val sharedPref = context.getSharedPreferences("wozart", Context.MODE_PRIVATE)
        val wallpaperId: Int = sharedPref.getInt(home, 0)
        if (wallpaperId > 12)
            return 0
        else return wallpaperId
    }

    private fun getWallpaperPath(context: Context): String {
        val sharedPref = context.getSharedPreferences("wozart", Context.MODE_PRIVATE)
        return sharedPref.getString(context.getString(R.string.saved_wallpaper_path_key), "").toString()
    }

    fun getThemeId(context: Context): Int {
        val sharedPref = context.getSharedPreferences("wozart", Context.MODE_PRIVATE)
        val themeId: Int = sharedPref.getInt(context.getString(R.string.saved_theme_key), 0)
        if (themeId != 0) {
            return themeId
        } else {
            Utils.saveWallPaperId(context, 0)
            return sharedPref.getInt(context.getString(R.string.saved_theme_key), 0)
        }
    }

    private fun createPalette(context: Context): androidx.palette.graphics.Palette = androidx.palette.graphics.Palette.from(getBitmapFromDrawable(context)).generate()

    private fun getBitmapFromDrawable(context: Context): Bitmap {
        val f = File(getWallpaperPath(context))
        if (f.exists()) {
            val bitmapSizeOptions = BitmapFactory.Options()
            bitmapSizeOptions.inJustDecodeBounds = true
            bitmapSizeOptions.inDither = false
            bitmapSizeOptions.inPurgeable = true
            bitmapSizeOptions.inInputShareable = true
            return BitmapFactory.decodeFile(f.absolutePath, bitmapSizeOptions)
        } else {
            return BitmapFactory.decodeResource(context.resources,
                    getWallpaperDrawables(getWallpaperId(context)))
        }
    }

    fun getWallpaperDrawables(position: Int): Int {
        val images = intArrayOf(R.drawable.one_new, R.drawable.two_new, R.drawable.three_new, R.drawable.four_new, R.drawable.five_new, R.drawable.six_new, R.drawable.seven_new, R.drawable.eight_new, R.drawable.nine_new)
        return images[position]

    }

    fun getRoomWallpaperDrawables(position: Int): Int {
        val images = intArrayOf(R.drawable.eleven_new, R.drawable.twelve_new, R.drawable.one_new, R.drawable.two_new, R.drawable.three_new, R.drawable.four_new, R.drawable.five_new, R.drawable.six_new, R.drawable.seven_new, R.drawable.eight_new, R.drawable.nine_new, R.drawable.ten_new, R.drawable.thirt_new)
        return images[position]

    }

    fun getThemeType(colorDark: Int, colorLight: Int): Int {
        var darkness = 1 - (0.299 * Color.red(colorDark) + 0.587 * Color.green(colorDark) + 0.114 * Color.blue(colorDark)) / 255
        if (darkness == 1.0)
            darkness = 0.0
        var lightness = 1 - (0.299 * Color.red(colorLight) + 0.587 * Color.green(colorLight) + 0.114 * Color.blue(colorLight)) / 255
        if (lightness == 1.0)
            lightness = 0.0
        if (darkness < lightness)
            return 0
        else return 1
    }

    private fun saveThemeType(context: Context) {
        val p = createPalette(context)
        val default = 0x000000
        val mutedLightVibrant = p.getLightVibrantColor(default)
        val mutedDarkVibrant = p.getDarkVibrantColor(default)
        if (getThemeType(mutedDarkVibrant, mutedLightVibrant) == 0) {
            //saveThemeId(context, R.style.AppThemeLight)
        } else {
            //saveThemeId(context, R.style.AppThemeDark)
        }
    }

    fun getOutputMediaFile(): File? {

        // External sdcard location
        val mediaStorageDir = File(
                Environment
                        .getExternalStorageDirectory(),
                "Wozart")

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {

                return null
            }
        }

        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMddHHmmss",
                Locale.getDefault()).format(Date())
        val mediaFile: File

        mediaFile = File(mediaStorageDir.path + File.separator
                + "WAP_" + timeStamp + ".jpeg")


        return mediaFile
    }

    fun getRemoteIcon(position: Int): Int {
        when (position) {
            0 -> return R.drawable.ic_power_off
            1 -> return R.drawable.ic_volume_mute
            2 -> return R.drawable.ic_tv_av
            3 -> return R.drawable.ic_favourite_button
            4 -> return R.drawable.remote_menu_button
            5 -> return R.drawable.ic_return_back
            6 -> return R.drawable.remote_info_button
            7 -> return R.drawable.remote_exit_button
            8 -> return R.drawable.number_remote_button
            9 -> return R.drawable.ic_fast_rewind_button
            10 -> return R.drawable.remote_pause_button
            11 -> return R.drawable.ic_fast_forward_remote
        }
        return position
    }


    fun getAppliancesImage(position: Int, type: String): Int {
        when (position) {
            position -> if (type == "TV") {
                return R.drawable.tv_images
            } else if (type == "AC") {
                return R.drawable.ac_images
            } else if (type == "Projector") {
                return R.drawable.projector_image
            } else if (type == "Home Theatres") {
                return R.drawable.home_threatre
            } else if (type == "Set-top box") {
                return R.drawable.settop_box
            } else if (type == "Camera") {
                return R.drawable.ic_camera
            }
        }
        return position
    }

    fun getSenseLoadsImages(position: Int): Int {
        when (position) {
            0 -> return R.drawable.ic_add_wallpaper_home
            1 -> return R.drawable.ic_motion_idle
            2 -> return R.drawable.ic_temperature
            3 -> return R.drawable.ic_humidity
            4 -> return R.drawable.ic_light_sensor
        }
        return position
    }

    fun getEditSenseLoadsImages(position: Int): Int {
        when (position) {
            0 -> return R.drawable.ic_motion_idle
            1 -> return R.drawable.ic_temperature
            2 -> return R.drawable.ic_humidity
            3 -> return R.drawable.ic_light_sensor
        }
        return position
    }

    fun getMotionImages(name: String): Int {
        when (name) {
            "Motion" -> {
                return R.drawable.ic_motion_triggered
            }
            "Temperature" -> {
                return R.drawable.ic_temperature_automation
            }
            "Humidity" -> {
                return R.drawable.ic_humidity_automation
            }
            "Light Intensity" -> {
                return R.drawable.ic_light_sensor_automation
            }
        }
        return R.drawable.ic_motion_triggered

    }

    fun getCurtainIcon(type: String, status: Boolean): Int {
        when (type) {
            CURTAIN -> if (status) {
                return R.drawable.ic_curtain_open
            } else {
                return R.drawable.ic_curtain_icons_curtain_close
            }
            else -> if (status) {
                return R.drawable.ic_switch_on_new_1
            } else {
                return R.drawable.ic_switch_on_new_1
            }
        }
    }

    fun getIconDrawable(type: Int, status: Boolean): Int {
        when (type) {
            BULB -> if (status) {
                return R.drawable.ic_bulb_on_new
            } else {
                return R.drawable.ic_bulb_off_new
            }
            LAMP -> if (status) {
                return R.drawable.ic_ceiling_lamp_on_new
            } else {
                return R.drawable.ic_ceiling_lamp_off_new
            }
            BED_LAMP -> if (status) {
                return R.drawable.ic_bed_lamp_on_new
            } else {
                return R.drawable.ic_bed_lamp_off_new
            }
            FAN -> if (status) {
                return R.drawable.fan_active_on
            } else {
                return R.drawable.ic_fan_off_
            }
            AC -> if (status) {
                return R.drawable.ic_ac_on
            } else {
                return R.drawable.ic_ac_off
            }
            EXAUSTFAN -> if (status) {
                return R.drawable.exhaust_fan_active
            } else {
                return R.drawable.ic_exhaust_fan_off
            }
            SOCKET -> if (status) {
                return R.drawable.ic_switch_on_new_1
            } else {
                return R.drawable.ic_switch_off_new_1
            }
            PLUG -> if (status) {
                return R.drawable.ic_smartplug_on
            } else {
                return R.drawable.ic_smartplug_off
            }
            else -> if (status) {
                return R.drawable.ic_switch_on_new_1
            } else {
                return R.drawable.ic_switch_on_new_1
            }
        }
    }


    fun getRoomDrawable(position: Int, selected: Boolean): Int {
        when (position) {
            0 -> if (selected) {
                return R.drawable.ic_living_room_on
            } else {
                return R.drawable.ic_living_room_off
            }
            else -> if (selected) {
                return R.drawable.ic_living_room_on
            } else {
                return R.drawable.ic_living_room_on
            }
        }

    }

    fun getSceneDrawable(position: Int, selected: Boolean): Int {
        val drawables = arrayListOf(R.drawable.ic_good_morning_off,
                R.drawable.ic_good_night_off, R.drawable.ic_enter_off, R.drawable.ic_exit_off, R.drawable.ic_movie_off, R.drawable.ic_party_off, R.drawable.ic_reading_off)
        val selecteImages = arrayListOf(R.drawable.ic_good_morning_on, R.drawable.ic_good_night_on, R.drawable.ic_enter_on, R.drawable.ic_exit_on, R.drawable.ic_movie_on, R.drawable.ic_party_on, R.drawable.ic_reading_on)
        if (selected) {
            return selecteImages[position]
        } else {
            return drawables[position]
        }

    }

    fun getSceneTestDrawable(position: Int, selected: Boolean): Int {
        when (position) {
            0 -> if (selected) {
                return R.drawable.ic_good_morning_scene_off
            } else {
                return R.drawable.ic_good_morning_off
            }
            1 -> if (selected) {
                return R.drawable.ic_good_night_scene_off
            } else {
                return R.drawable.ic_good_night_off
            }
            2 -> if (selected) {
                return R.drawable.ic_enter_scene_on
            } else {
                return R.drawable.ic_enter_off
            }
            3 -> if (selected) {
                return R.drawable.ic_exit_scene_off
            } else {
                return R.drawable.ic_exit_off
            }
            4 -> if (selected) {
                return R.drawable.ic_movie_scene_off
            } else {
                return R.drawable.ic_movie_off
            }
            5 -> if (selected) {
                return R.drawable.ic_party_scene_off
            } else {
                return R.drawable.ic_party_off
            }
            6 -> if (selected) {
                return R.drawable.ic_reading_scene_off
            } else {
                return R.drawable.ic_reading_off
            }

            else -> if (selected) {
                return R.drawable.ic_good_morning_scene_off
            } else {
                return R.drawable.ic_good_morning_off
            }
        }
    }

    fun getButtonText(context: Context, name: String): String {
        if (name == "KEY_1") {
            return context.getString(R.string._1)
        } else if (name == "KEY_2") {
            return context.getString(R.string._2)
        } else if (name == "KEY_3") {
            return context.getString(R.string._3)
        } else if (name == "KEY_4") {
            return context.getString(R.string._4)
        } else if (name == "KEY_5") {
            return context.getString(R.string._5)
        } else if (name == "KEY_6") {
            return context.getString(R.string._6)
        } else if (name == "KEY_7") {
            return context.getString(R.string._7)
        } else if (name == "KEY_8") {
            return context.getString(R.string._8)
        } else if (name == "KEY_9") {
            return context.getString(R.string._9)
        } else if (name == "KEY_0") {
            return context.getString(R.string._0)
        } else if (name == "ON") {
            return "ON"
        }
        return context.getString(R.string._1)
    }

    fun getRemoteIconByName(name: String): Int {
        if (name == "KEY_POWER") {
            return R.drawable.ic_ir_remote_icons_on_icon
        } else if (name == "KEY_MUTE") {
            return R.drawable.ic_ir_remote_icons_mute
        } else if (name == "KEY_TV") {
            return R.drawable.ic_ir_remote_icons_tv_av
        } else if (name == "KEY_VOLUMEUP") {
            return R.drawable.ic_ir_remote_icons_volume_up
        } else if (name == "KEY_VOLUMEDOWN") {
            return R.drawable.ic_ir_remote_icons_volume_down
        } else if (name == "KEY_CHANNELUP") {
            return R.drawable.ic_remote_icons__channel
        } else if (name == "KEY_CHANNELDOWN") {
            return R.drawable.ic_remote_icons__channel_down
        } else if (name == "KEY_FAVORITES") {
            return R.drawable.ic_remote_icons__favourites
        } else if (name == "KEY_MENU") {
            return R.drawable.ic_ir_remote_icons_menu
        } else if (name == "KEY_EXIT") {
            return R.drawable.ic_remote_icons__exit
        } else if (name == "KEY_INFO") {
            return R.drawable.ic_remote_icons__info
        } else if (name == "KEY_INTERNET") {
            return R.drawable.ic_ir_remote_icons_internet
        } else if (name == "KEY_RETURN") {
            return R.drawable.ic_ir_remote_icons_back
        } else if (name == "KEY_REWIND") {
            return R.drawable.ic_fast_rewind_button
        } else if (name == "KEY_PAUSE") {
            return R.drawable.remote_pause_button
        } else if (name == "KEY_FORWARD") {
            return R.drawable.ic_fast_forward_remote
        } else if (name == "KEY_PLAY") {
            return R.drawable.remote_play_button
        } else if (name == "KEY_STOP") {
            return R.drawable.ic_stop_black_24dp
        } else if (name == "KEY_RECORD") {
            return R.drawable.remote_play_button
        } else if (name == "KEY_RED") {
            return R.drawable.ic_red
        } else if (name == "KEY_GREEN") {
            return R.drawable.ic_green_button
        } else if (name == "KEY_BLUE") {
            return R.drawable.ic_blue
        } else if (name == "KEY_BACK") {
            return R.drawable.ic_remote_icons__exit
        } else if (name == "Tools") {
            return R.drawable.ic_settings_black_24dp
        } else if (name == "Netflix") {
            return R.drawable.netflix
        } else if (name == "Hotstar") {
            return R.drawable.hotstar
        } else if (name == "YouTube") {
            return R.drawable.youtube
        } else if (name == "Amazon Prime") {
            return R.drawable.amazoneprime
        } else if (name == "KEY_DOWN") {
            return R.drawable.ic_keyboard_arrow_down_black_24dp
        } else if (name == "KEY_UP") {
            return R.drawable.ic_baseline_keyboard_arrow_up_24
        } else if (name == "KEY_RIGHT") {
            return R.drawable.ic_keyboard_arrow_right_black_24dp
        } else if (name == "KEY_LEFT") {
            return R.drawable.ic_keyboard_arrow_left_black_24px
        } else if (name == "KEY_OK") {
            return R.drawable.ic_keyboard_arrow_down_black_24dp
        }
        return R.drawable.ic_ir_remote_icons_on_icon
    }

    fun getAcRemoteButtonIcon(name: String): Int {
        if (name == "Heating") {
            return R.drawable.ic_air_conditioner_heat_mode
        } else if (name == "Cooling") {
            return R.drawable.ic_air_conditioner_cool_mode
        } else if (name == "KEY_POWER") {
            return R.drawable.ic_ir_remote_icons_on_icon
        } else if (name == "Swing_Mode") {
            return R.drawable.ic_fan_mode
        }
        return R.drawable.ic_fan_mode
    }

    fun getDeviceIcon(context: Context, name: String): Int {
        if (name == context.getString(R.string.aura_switch_product)) {
            return R.drawable.ic_wozart_switch_controller_new
        } else if (name == context.getString(R.string.aura_sense_product)) {
            return R.drawable.ic_wozart_sense_new
        } else if (name == Constant.DEVICE_UNIVERSAL_IR) {
            return R.drawable.aura_sense_design
        } else if (name == context.getString(R.string.aura_plug_product)) {
            return R.drawable.ic_wozart_smart_plug_new
        } else if (name == context.getString(R.string.aura_button_product)) {
            return R.drawable.ic_wozart_scene_controller_new
        } else if (name == context.getString(R.string.aura_curtain_product) || name == context.getString(R.string.gate_controller)) {
            return R.drawable.ic_wozart_switch_controller_mini_new
        } else if (name == context.getString(R.string.wozart_led)) {
            return R.drawable.ic_wozart_led_orchestrator
        } else if (name == context.getString(R.string.other_device)) {

        }
        return 0
    }

    fun getDashboardBackgroud(context: Context, name: String): Int {
        if (name == context.getString(R.string.good_morning)) {
            return R.drawable.good_morning_dashboard
        } else if (name == context.getString(R.string.good_noon)) {
            return R.drawable.good_afternoon_dashboard
        } else if (name == context.getString(R.string.good_evening)) {
            return R.drawable.good_evening_dashboard
        } else if (name == context.getString(R.string.good_night)) {
            return R.drawable.good_night_new
        } else {
            return R.drawable.good_morning_dashboard
        }
    }

    fun getDashboardTutorial(viewList: ArrayList<View>): ArrayList<TutorialModel> {
        val tutorialList = arrayListOf<TutorialModel>()
        for (view in viewList) {
            val positionArray = IntArray(2)
            view.getLocationInWindow(positionArray)
            val tutInfo = TutorialModel()
            tutInfo.x = positionArray[0] + view.width / 2f
            tutInfo.y = positionArray[1] + view.height / 2f
            tutInfo.height = view.height.toFloat()
            tutInfo.width = view.width.toFloat()
            when (view.id) {
                R.id.action_item1 -> {
                    // val hometab = TutorialModel()
                    tutInfo.id = view
                    tutInfo.title = view.context.getString(
                            R.string.all_homes
                    )
                    tutInfo.message = String.format(
                            view.context.getString(
                                    R.string.home_tab_info
                            )
                    )
                }
                R.id.action_item2 -> {
                    //val roomTab = TutorialModel()
                    tutInfo.id = view
                    tutInfo.title = view.context.getString(R.string.all_rooms)
                    tutInfo.message = view.context.getString(
                            R.string.room_tab_info
                    )
                }
                R.id.action_item3 -> {
                    //val automationTab = TutorialModel()
                    tutInfo.id = view
                    tutInfo.title = view.context.getString(R.string.text_title_create_automation)
                    tutInfo.message = view.context.getString(
                            R.string.automation_info
                    )
                }
                R.id.action_item4 -> {
                    // val settingsTab = TutorialModel()
                    tutInfo.id = view
                    tutInfo.title = view.context.getString(R.string.settings)
                    tutInfo.message = view.context.getString(
                            R.string.settings_tab_info
                    )
                }
            }
            tutInfo.overLayY = tutInfo.y!! + tutInfo.radius
            if (tutInfo.id != null && view.visibility == View.VISIBLE) {
                tutorialList.add(tutInfo)
            }
        }
        return tutorialList
    }

}
