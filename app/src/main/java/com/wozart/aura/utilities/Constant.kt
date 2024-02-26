package com.wozart.aura.utilities

import com.wozart.aura.entity.sql.buttonDevice.ButtonDbContract
import com.wozart.aura.entity.sql.camera.CameraContract
import com.wozart.aura.entity.sql.device.DeviceContract
import com.wozart.aura.entity.sql.scenes.SceneContract
import com.wozart.aura.entity.sql.scheduling.ScheduleContract
import com.wozart.aura.entity.sql.sense.AuraSenseContract
import com.wozart.aura.entity.sql.users.UserContract
import com.wozart.aura.entity.sql.utils.UtilsContract

class Constant {
    companion object {
        //User's and Device Constants
        var EMAIL: String? = null
        var IDENTITY_ID: String? = null
        var USERNAME: String? = null
        var HOME: String? = null
        var USER_ID: String? = null
        val UNPAIRED = "xxxxxxxxxxxxxxxxx"
        var NOTIFICATION_COUNT = 0
        var IS_FIRST = true
        var IS_LOGOUT = false
        const val OFFLINE_MODE = "offline"
        const val ONLINE_MODE = "ONLINE"
        const val NEW_USER = "newUser"
        const val EXIST_USER = "existUser"
        var GOOGLE_LOGIN_EXIST = true
        var TIME_ZONE = "-5:30"
        var ACCOUNT_VERIFIED :String ?= null
        const val DOWNLOAD = "download_remote"
        const val SEARCH_DATA = "search_remote"

        //for google map
        private val PACKAGE_NAME = "com.wozart.aura.ui.createautomation"
        val GEOFENCE_POSITION_LATITUDE = "GEOFENCE POSITION LATITUDE"
        val CURRENT_LATITUDE = "GEOFENCE CURRENT LATITUDE"
        val CURRENT_LONGITUDE = "GEOFENCE CURRENT LONGITUDE"
        val GEOFENCE_POSITION_LONGITUDE = "GEOFENCE POSITION LONGITUDE"


        //SQL - Lite Queries for Device DB
        val GET_ALL_LOADS = "select " + DeviceContract.DeviceEntry.DEVICE + " from " + DeviceContract.DeviceEntry.TABLE_NAME
        val GET_LOAD = "select " + DeviceContract.DeviceEntry.DEVICE + " , " + DeviceContract.DeviceEntry.LOAD + " , " + DeviceContract.DeviceEntry.THING + " , " + DeviceContract.DeviceEntry.ROOM + " , " + DeviceContract.DeviceEntry.UIUD + " from " + DeviceContract.DeviceEntry.TABLE_NAME + " where " + DeviceContract.DeviceEntry.DEVICE + " =?"
        val GET_ALL_LOADS_HOME = "select " + DeviceContract.DeviceEntry.DEVICE + " from " + DeviceContract.DeviceEntry.TABLE_NAME + " where " + DeviceContract.DeviceEntry.HOME + " = ?"
        val GET_ALL_HOME = "select distinct " + DeviceContract.DeviceEntry.HOME + " from " + DeviceContract.DeviceEntry.TABLE_NAME
        val GET_ROOMS_QUERY = "select distinct " + DeviceContract.DeviceEntry.ROOM + " from " + DeviceContract.DeviceEntry.TABLE_NAME + " where " + DeviceContract.DeviceEntry.HOME + " = ?"
        val GET_ROOMS_QUERY_NEW = "select distinct " + DeviceContract.DeviceEntry.ROOM + " , " + DeviceContract.DeviceEntry.DEVICE + " from " + DeviceContract.DeviceEntry.TABLE_NAME + " where " + DeviceContract.DeviceEntry.HOME + " = ?"
        val INSERT_ROOMS_QUERY = "select " + DeviceContract.DeviceEntry.HOME + " from " + DeviceContract.DeviceEntry.TABLE_NAME + " where " + DeviceContract.DeviceEntry.ROOM + " = ?"
        val CRUD_ROOM = DeviceContract.DeviceEntry.HOME + " =? and " + DeviceContract.DeviceEntry.ROOM + " =? "
        val GET_DEVICES_IN_ROOM_QUERY = "select " + DeviceContract.DeviceEntry.DEVICE + " , " + DeviceContract.DeviceEntry.LOAD + " , " + DeviceContract.DeviceEntry.THING + " from " + DeviceContract.DeviceEntry.TABLE_NAME + " where " + DeviceContract.DeviceEntry.HOME + " = ? and " + DeviceContract.DeviceEntry.ROOM + " =?"
        val UPDATE_ROOM_DETAILS = DeviceContract.DeviceEntry.ROOM + "=?"

        val GET_DEVICE_TABLE = "select " + DeviceContract.DeviceEntry.LOAD + " , " + DeviceContract.DeviceEntry.HOME + " , " + DeviceContract.DeviceEntry.ROOM + " ," + DeviceContract.DeviceEntry.THING + " , " + DeviceContract.DeviceEntry.UIUD + " , " + DeviceContract.DeviceEntry.ACCESS + " , " + DeviceContract.DeviceEntry.DEVICE + " from " + DeviceContract.DeviceEntry.TABLE_NAME
        val GET_DEVICES_IN_HOME_QUERY = "select " + DeviceContract.DeviceEntry.DEVICE + " , " + DeviceContract.DeviceEntry.LOAD + " , " + DeviceContract.DeviceEntry.THING + " , " + DeviceContract.DeviceEntry.UIUD + " from " + DeviceContract.DeviceEntry.TABLE_NAME + " where " + DeviceContract.DeviceEntry.HOME + " = ? "
        val GET_ALL_LOADS_SCENES = "select " + DeviceContract.DeviceEntry.DEVICE + " , " + DeviceContract.DeviceEntry.LOAD + " , " + DeviceContract.DeviceEntry.ROOM + " from " + DeviceContract.DeviceEntry.TABLE_NAME + " where " + DeviceContract.DeviceEntry.HOME + " = ? "

        val GET_LOADS_JSON = "select " + DeviceContract.DeviceEntry.LOAD + " from " + DeviceContract.DeviceEntry.TABLE_NAME + " where " + DeviceContract.DeviceEntry.DEVICE + " = ?"
        val CHECK_ADD_QUERY = "select " + DeviceContract.DeviceEntry.DEVICE + " from " + DeviceContract.DeviceEntry.TABLE_NAME + " where " + DeviceContract.DeviceEntry.DEVICE + " = ?"
        val CHECK_DEVICES = "select " + DeviceContract.DeviceEntry.DEVICE + " from " + DeviceContract.DeviceEntry.TABLE_NAME + " where " + DeviceContract.DeviceEntry.DEVICE + " = ?"
        val GET_THING_NAME_QUERY = "select " + DeviceContract.DeviceEntry.THING + " from " + DeviceContract.DeviceEntry.TABLE_NAME
        val GET_DEVICES_FOR_THING_QUERY = "select " + DeviceContract.DeviceEntry.DEVICE + " from " + DeviceContract.DeviceEntry.TABLE_NAME + " where " + DeviceContract.DeviceEntry.THING + " = ?"
        val GET_THING_FOR_DEVICES_QUERY = "select " + DeviceContract.DeviceEntry.THING + " from " + DeviceContract.DeviceEntry.TABLE_NAME + " where " + DeviceContract.DeviceEntry.DEVICE + " = ?"
        val UPDATE_DEVICE = DeviceContract.DeviceEntry.DEVICE + "=?"
        val UPDATE_LOAD = DeviceContract.DeviceEntry.DEVICE + "=?"
        val GET_ROOM_FOR_DEVICE_QUERY = "select " + DeviceContract.DeviceEntry.ROOM + " from " + DeviceContract.DeviceEntry.TABLE_NAME + " where " + DeviceContract.DeviceEntry.DEVICE + " =?"
        val DELETE_DEVICE_QUERY = DeviceContract.DeviceEntry.DEVICE + " = ?"
        val DELETE_HOME_QUERY = DeviceContract.DeviceEntry.HOME + " = ?"
        val DELETE_HOME = DeviceContract.DeviceEntry.HOME + " =?"
        val GET_UIUD_QUERY = "select " + DeviceContract.DeviceEntry.UIUD + " from " + DeviceContract.DeviceEntry.TABLE_NAME + " where " + DeviceContract.DeviceEntry.DEVICE + " = ?"
        val UPDATE_UIUD = DeviceContract.DeviceEntry.DEVICE + " = ? "
        val GET_UIUD_QUERY_ALL = "select " + DeviceContract.DeviceEntry.UIUD + " , " + DeviceContract.DeviceEntry.DEVICE + " from " + DeviceContract.DeviceEntry.TABLE_NAME

        //SQL -Lite Queries for Favourites
        val GET_ALL_FAV_QUERY = "select " + DeviceContract.DeviceEntry.LOAD + ", " + DeviceContract.DeviceEntry.DEVICE + " from " + DeviceContract.DeviceEntry.TABLE_NAME + " where " + DeviceContract.DeviceEntry.HOME + " =?"

        //SQL- Lite Queries for Scene
        val CHECK_SCENE_QUERY = "select " + SceneContract.SceneEntry.SCENE_ID + " from " + SceneContract.SceneEntry.TABLE_NAME + " where " + SceneContract.SceneEntry.SCENE_ID + " =?"
        val UPDATE_SCENE_PARAMS = SceneContract.SceneEntry.SCENE_ID + "=?"
        val GET_SCENE_QUERY = "select " + SceneContract.SceneEntry.SCENE_ID + " , " + SceneContract.SceneEntry.LOAD + " , " + SceneContract.SceneEntry.ICON + " , " + SceneContract.SceneEntry.REMOTE + " from " + SceneContract.SceneEntry.TABLE_NAME + " where " + SceneContract.SceneEntry.HOME + " = ? "
        val GET_SCENE_FOR_ROOM_QUERY = "select " + SceneContract.SceneEntry.SCENE_ID + " , " + SceneContract.SceneEntry.LOAD + " , " + SceneContract.SceneEntry.ROOM + " , " + SceneContract.SceneEntry.ICON + " from " + SceneContract.SceneEntry.TABLE_NAME + " where " + SceneContract.SceneEntry.HOME + " = ? and " + SceneContract.SceneEntry.ROOM + " =?"
        val DELETE_SCENE = SceneContract.SceneEntry.SCENE_ID + " = ? and " + SceneContract.SceneEntry.HOME + " =?"
        val GET_SELECTED_SCENE = "select " + SceneContract.SceneEntry.SCENE_ID + " , " + SceneContract.SceneEntry.LOAD + " , " + SceneContract.SceneEntry.ICON + " , " + SceneContract.SceneEntry.REMOTE + " from " + SceneContract.SceneEntry.TABLE_NAME + " where " + SceneContract.SceneEntry.SCENE_ID + " = ? and " + SceneContract.SceneEntry.HOME + " =?"
        val DELETE_HOME_SCENE_QUERY = SceneContract.SceneEntry.HOME + " = ?"
        val GET_ALL_SCENES = "select " + SceneContract.SceneEntry.HOME + " , " + SceneContract.SceneEntry.LOAD + " , " + SceneContract.SceneEntry.SCENE_ID + " , " + SceneContract.SceneEntry.ROOM + " , " + SceneContract.SceneEntry.ICON + " , " + SceneContract.SceneEntry.REMOTE + " from " + SceneContract.SceneEntry.TABLE_NAME
        val UPDATE_ROOM_DETAILS_SCENE = SceneContract.SceneEntry.ROOM + " =?"
        val CHECK_SCENE_EXIST_QUERY = "select " + SceneContract.SceneEntry.SCENE_ID + " from " + SceneContract.SceneEntry.TABLE_NAME + " where " + SceneContract.SceneEntry.SCENE_ID + " =? and " + SceneContract.SceneEntry.HOME + " =?"

        //SQLite queries for Aura Sense
        val CHECK_REMOTE_FOR_DEVICE = "select " + AuraSenseContract.AuraSenseEntry.REMOTE_NAME + " , " + AuraSenseContract.AuraSenseEntry.REMOTE_MODEL + " from " + AuraSenseContract.AuraSenseEntry.TABLE_NAME + " where " + AuraSenseContract.AuraSenseEntry.REMOTE_NAME + " = ? and " + AuraSenseContract.AuraSenseEntry.REMOTE_LOCATION + " =?"
        val GET_REMOTE_DATA = "selcet " + AuraSenseContract.AuraSenseEntry.REMOTE_NAME + " , " + AuraSenseContract.AuraSenseEntry.REMOTE_BRAND + " , " + AuraSenseContract.AuraSenseEntry.REMOTE_MODEL + " , " + AuraSenseContract.AuraSenseEntry.BUTTON_ICON + " , " + AuraSenseContract.AuraSenseEntry.REMOTE_FOVOURITE + " , " + AuraSenseContract.AuraSenseEntry.REMOTE_LOCATION + " from " + AuraSenseContract.AuraSenseEntry.TABLE_NAME + " where " + AuraSenseContract.AuraSenseEntry.REMOTE_NAME + " = ? and " + AuraSenseContract.AuraSenseEntry.REMOTE_MODEL + " =? "
        val GET_REMOTE_LIST = "select " + AuraSenseContract.AuraSenseEntry.REMOTE_BRAND + " , " + AuraSenseContract.AuraSenseEntry.REMOTE_NAME + " , " + AuraSenseContract.AuraSenseEntry.REMOTE_MODEL + " , " + AuraSenseContract.AuraSenseEntry.REMOTE_LOCATION + " , " + AuraSenseContract.AuraSenseEntry.BUTTON_ICON + " , " + AuraSenseContract.AuraSenseEntry.REMOTE_FOVOURITE + " , " + AuraSenseContract.AuraSenseEntry.DEVICE_NAME + " , " + AuraSenseContract.AuraSenseEntry.APPLIANCE_TYPE + " from " + AuraSenseContract.AuraSenseEntry.TABLE_NAME + " where " + AuraSenseContract.AuraSenseEntry.DEVICE_NAME + " = ? "
        val DELETE_SENSE_TABLE = AuraSenseContract.AuraSenseEntry.REMOTE_NAME + " =? and " + AuraSenseContract.AuraSenseEntry.REMOTE_LOCATION + " =?"
        val GET_FAVOURITE_REMOTE = "select " + AuraSenseContract.AuraSenseEntry.REMOTE_NAME + " , " + AuraSenseContract.AuraSenseEntry.REMOTE_BRAND + " , " + AuraSenseContract.AuraSenseEntry.REMOTE_MODEL + " , " + AuraSenseContract.AuraSenseEntry.DEVICE_NAME + " , " + AuraSenseContract.AuraSenseEntry.BUTTON_ICON + " from " + AuraSenseContract.AuraSenseEntry.TABLE_NAME + " where " + AuraSenseContract.AuraSenseEntry.HOME_ASSOCIATE + " =? "
        val DELETE_REMOTE = AuraSenseContract.AuraSenseEntry.REMOTE_NAME + " = ? and " + AuraSenseContract.AuraSenseEntry.DEVICE_NAME + " =? "
        val DELETE_REMOTE_SENSE = AuraSenseContract.AuraSenseEntry.DEVICE_NAME + " = ? "

        //sql-lite query for Automation Scheduling
        val AUTOMATION_SCENE_QUERY = "select " + ScheduleContract.ScheduleEntry.SCHEDULE_NAME + " from " + ScheduleContract.ScheduleEntry.TABLE_NAME + " where " + ScheduleContract.ScheduleEntry.SCHEDULE_NAME + " =?"
        val UPDATE_AUTOMATION_SCENE = ScheduleContract.ScheduleEntry.SCHEDULE_NAME + " =?"
        val GET_AUTOMATION_SCENE_QUERY = "select " + ScheduleContract.ScheduleEntry.SCHEDULE_NAME + " , " + ScheduleContract.ScheduleEntry.ICON + " , " + ScheduleContract.ScheduleEntry.ROOM + " , " + ScheduleContract.ScheduleEntry.LOAD + " , " + ScheduleContract.ScheduleEntry.SCHEDULE_PROPERTY + " , " + ScheduleContract.ScheduleEntry.START_TIME + " , " + ScheduleContract.ScheduleEntry.SCHEDULE_TYPE + " , " + ScheduleContract.ScheduleEntry.END_TIME + " , " + ScheduleContract.ScheduleEntry.ROUTINE + " , " + ScheduleContract.ScheduleEntry.REMOTE + " from " + ScheduleContract.ScheduleEntry.TABLE_NAME + " where " + ScheduleContract.ScheduleEntry.HOME + " =?"
        val CHECK_AUTOMATION_QUERY = "select " + ScheduleContract.ScheduleEntry.SCHEDULE_NAME + " , " + ScheduleContract.ScheduleEntry.ICON + " , " + ScheduleContract.ScheduleEntry.ROOM + " , " + ScheduleContract.ScheduleEntry.LOAD + " , " + ScheduleContract.ScheduleEntry.SCHEDULE_PROPERTY + " , " + ScheduleContract.ScheduleEntry.START_TIME + " , " + ScheduleContract.ScheduleEntry.SCHEDULE_TYPE + " , " + ScheduleContract.ScheduleEntry.END_TIME + " , " + ScheduleContract.ScheduleEntry.ROUTINE + " , " + ScheduleContract.ScheduleEntry.REMOTE + " from " + ScheduleContract.ScheduleEntry.TABLE_NAME + " where " + ScheduleContract.ScheduleEntry.SCHEDULE_NAME + " =? and " + ScheduleContract.ScheduleEntry.HOME + " =?"
        val GET_AUTOMATION_SELECTED_SCENE = " select" + ScheduleContract.ScheduleEntry.SCHEDULE_NAME + " , " + ScheduleContract.ScheduleEntry.ICON + " , " + ScheduleContract.ScheduleEntry.ROOM + " , " + ScheduleContract.ScheduleEntry.LOAD + " from " + ScheduleContract.ScheduleEntry.TABLE_NAME + " where " + ScheduleContract.ScheduleEntry.SCHEDULE_NAME + " =?"
        val DELETE_AUTOMATION_SCENE = ScheduleContract.ScheduleEntry.SCHEDULE_NAME + " =? and " + ScheduleContract.ScheduleEntry.HOME + " =? "
        val GET_AUTOMATION_SCENE_QUERY_NAME = "select " + ScheduleContract.ScheduleEntry.SCHEDULE_NAME + " , " + ScheduleContract.ScheduleEntry.ICON + " , " + ScheduleContract.ScheduleEntry.ROOM + " , " + ScheduleContract.ScheduleEntry.LOAD + " , " + ScheduleContract.ScheduleEntry.SCHEDULE_PROPERTY + " , " + ScheduleContract.ScheduleEntry.START_TIME + " , " + ScheduleContract.ScheduleEntry.SCHEDULE_TYPE + " , " + ScheduleContract.ScheduleEntry.END_TIME + " , " + ScheduleContract.ScheduleEntry.ROUTINE + " , " + ScheduleContract.ScheduleEntry.REMOTE + " from " + ScheduleContract.ScheduleEntry.TABLE_NAME + " where " + ScheduleContract.ScheduleEntry.SCHEDULE_NAME + " =? "
        val DELETE_HOME_SCENES = ScheduleContract.ScheduleEntry.HOME + " = ? "

        val AUTOMATION_EXIST_QUERY = "select " + ScheduleContract.ScheduleEntry.SCHEDULE_NAME + " from " + ScheduleContract.ScheduleEntry.TABLE_NAME + " where " + ScheduleContract.ScheduleEntry.SCHEDULE_NAME + " =? and " + ScheduleContract.ScheduleEntry.HOME + " =?"

        //Aura Button Table
        val CHECK_BUTTON_QUERIES = "select " + ButtonDbContract.ButtonDbEntry.BUTTON_NAME + " from " + ButtonDbContract.ButtonDbEntry.TABLE_NAME + " where " + ButtonDbContract.ButtonDbEntry.BUTTON_NAME + " =? and " + ButtonDbContract.ButtonDbEntry.BUTTON_ID + " =?"
        val DELETE_EXISTING_BUTTON = ButtonDbContract.ButtonDbEntry.BUTTON_NAME + " =? and " + ButtonDbContract.ButtonDbEntry.BUTTON_ID + " =?"
        val GET_BUTTON_DATA = "select " + ButtonDbContract.ButtonDbEntry.BUTTON_NAME + " , " + ButtonDbContract.ButtonDbEntry.BUTTON_ID + " , " + ButtonDbContract.ButtonDbEntry.BUTTON_TAP + " , " + ButtonDbContract.ButtonDbEntry.LOAD + " , " + ButtonDbContract.ButtonDbEntry.BUTTON_UNICAST + " , " + ButtonDbContract.ButtonDbEntry.ROOM + " , " + ButtonDbContract.ButtonDbEntry.HOME + " , " + ButtonDbContract.ButtonDbEntry.SENSE_UIUD + " , " + ButtonDbContract.ButtonDbEntry.SENSE_NAME + " , " + ButtonDbContract.ButtonDbEntry.SENSE_THING + " from " + ButtonDbContract.ButtonDbEntry.TABLE_NAME + " where " + ButtonDbContract.ButtonDbEntry.BUTTON_NAME + " =? and " + ButtonDbContract.ButtonDbEntry.BUTTON_ID + " =?"
        val GET_ALL_BUTTON = "select " + ButtonDbContract.ButtonDbEntry.BUTTON_NAME + " , " + ButtonDbContract.ButtonDbEntry.BUTTON_ID + " , " + ButtonDbContract.ButtonDbEntry.BUTTON_TAP + " , " + ButtonDbContract.ButtonDbEntry.LOAD + " , " + ButtonDbContract.ButtonDbEntry.BUTTON_UNICAST + " , " + ButtonDbContract.ButtonDbEntry.ROOM + " , " + ButtonDbContract.ButtonDbEntry.HOME + " , " + ButtonDbContract.ButtonDbEntry.SENSE_UIUD + " , " + ButtonDbContract.ButtonDbEntry.SENSE_NAME + " , " + ButtonDbContract.ButtonDbEntry.SENSE_THING + " from " + ButtonDbContract.ButtonDbEntry.TABLE_NAME + " where " + ButtonDbContract.ButtonDbEntry.BUTTON_NAME + " =?"
        val DELETE_BUTTON_QUERY = ButtonDbContract.ButtonDbEntry.BUTTON_NAME + " = ?"
        val GET_SCENE_ASSIGN_BUTTON = "select " + ButtonDbContract.ButtonDbEntry.BUTTON_NAME + " , " + ButtonDbContract.ButtonDbEntry.BUTTON_ID + " , " + ButtonDbContract.ButtonDbEntry.BUTTON_TAP + " , " + ButtonDbContract.ButtonDbEntry.LOAD + " , " + ButtonDbContract.ButtonDbEntry.BUTTON_UNICAST + " , " + ButtonDbContract.ButtonDbEntry.ROOM + " , " + ButtonDbContract.ButtonDbEntry.HOME + " , " + ButtonDbContract.ButtonDbEntry.SENSE_UIUD + " , " + ButtonDbContract.ButtonDbEntry.SENSE_NAME + " , " + ButtonDbContract.ButtonDbEntry.SENSE_THING + " from " + ButtonDbContract.ButtonDbEntry.TABLE_NAME + " where " + ButtonDbContract.ButtonDbEntry.BUTTON_NAME + " =? and " + ButtonDbContract.ButtonDbEntry.BUTTON_ID + " =?"

        //Utils Table
        val GET_KEY_VALUE_QUERY = "select " + UtilsContract.UtilsEntry.VALUE + " from " + UtilsContract.UtilsEntry.TABLE_NAME + " where " + UtilsContract.UtilsEntry.KEY + " =?"
        val UPDATE_KEY_VALUE_QUERY = UtilsContract.UtilsEntry.KEY + "=?"
        val GET_ALL_AUTOMATION = "select " + ScheduleContract.ScheduleEntry.SCHEDULE_NAME + " , " + ScheduleContract.ScheduleEntry.ICON + " , " + ScheduleContract.ScheduleEntry.ROOM + " , " + ScheduleContract.ScheduleEntry.LOAD + " , " + ScheduleContract.ScheduleEntry.SCHEDULE_PROPERTY + " , " + ScheduleContract.ScheduleEntry.START_TIME + " , " + ScheduleContract.ScheduleEntry.SCHEDULE_TYPE + " , " + ScheduleContract.ScheduleEntry.END_TIME + " , " + ScheduleContract.ScheduleEntry.ROUTINE + " , " + ScheduleContract.ScheduleEntry.REMOTE + " , " + ScheduleContract.ScheduleEntry.HOME + " from " + ScheduleContract.ScheduleEntry.TABLE_NAME

        //USER-TABLE
        val CHECK_USER_PRESENCE = "select " + UserContract.UserEntry.USER_ID + " from " + UserContract.UserEntry.TABLE_NAME + " where " + UserContract.UserEntry.USER_ID + " =?"



        val DELETE_USER_TABLE = UserContract.UserEntry.USER_ID + " =?"

        val GET_USER_LIST = "select " + UserContract.UserEntry.USER_FIRSTNAME + " , " + UserContract.UserEntry.USER_LASTNAME + " , " + UserContract.UserEntry.USER_CONTACT + " , " + UserContract.UserEntry.USER_PROFILE + " from " + UserContract.UserEntry.TABLE_NAME + " where " + UserContract.UserEntry.USER_ID + " =?"

        //Camera Table
        val GET_CAMERA_DATA = "select " + CameraContract.CameraEntry.CAMERA_NAME + " , " + CameraContract.CameraEntry.HOME + " from " + CameraContract.CameraEntry.TABLE_NAME + " where " + CameraContract.CameraEntry.CAMERA_NAME + " = ? and " + CameraContract.CameraEntry.HOME + " =?"
        val GET_CAMERA = "select " + CameraContract.CameraEntry.CAMERA_NAME + " , " + CameraContract.CameraEntry.HOME + " , " + CameraContract.CameraEntry.ROOM + " , " + CameraContract.CameraEntry.CAMERA_URL + " , " + CameraContract.CameraEntry.FAVOURITE + " from " + CameraContract.CameraEntry.TABLE_NAME + " where " + CameraContract.CameraEntry.HOME + " =?"
        val DELETE_CAMERA_ = CameraContract.CameraEntry.CAMERA_NAME + " = ? and " + CameraContract.CameraEntry.HOME + " =?"

        //AWS MQTT Messages
        val AWS_UPDATE_ACCEPTED = "\$aws/things/%s/shadow/update/accepted"
        val AWS_GET_ACCEPTED = "\$aws/things/%s/shadow/get/accepted"
        val AWS_UPDATE = "\$aws/things/%s/shadow/update"
        val AWS_GET = "\$aws/things/%s/shadow/get"

        //tab Ids
        val HOME_TAB = 0
        val ROOMS_TAB = 1
        val AUTOMATION_TAB = 2
        val MORE_TAB = 3

        //DAYS
        val SUNDAY = "Sunday"
        val MONDAY = "Monday"
        val TUESDAY = "Tuesday"
        val WEDNESDAY = "Wednesday"
        val THURSDAY = "Thursday"
        val FRIDAY = "Friday"
        val SATURDAY = "Saturday"

        //App
        val CREATE_HOME_ROOM = "create_home_room"
        val CREATE_ROOM_NAME = "create_room"
        val SHARE_HOME = 0
        val EDIT_HOME = 1
        val SELECTED_HOME = 2

        //Device Types
        val BULB = 0
        val LAMP = 1
        val BED_LAMP = 2
        val FAN = 3
        val SOCKET = 4
        val AC = 5
        val EXAUSTFAN = 6
        val PLUG = 7
        val CURTAIN_ICON = 8
        var TEMP: String? = null
        var HUMIDITY: String? = null
        var LUX: String? = null
        const val ROOM_CREATION = "new_room"
        const val NEW_ROOM = "room"
        const val SET_LOCATION_HOME = "location"
        const val SET_LOCATION = "home_location"
        const val REMOTE = "remote_learn"
        const val DEVICE_TYPE = "zmote"
        const val BUTTON_ROOM = "aura_btn_room"
        const val BUTTON_NAME = "aura_btn_name"
        const val BUTTON_DEVICE_DATA = "btn_data"
        const val SCENE_BUTTON_FRAGMENT = "scene_button_fragment"
        const val PARAM_FRAGMENT = "fragment"
        const val BUTTON_PRESSED_FRAGMENT = "button_pressed_fragment"
        const val SET_ACTION_FRAGMENT = "set_action_fragment"
        const val BUTTON_PRESSED_DATA = "btn_pressed_data"
        const val SELECT_LOAD_FRAGMENT = "select_fragment"
        const val BUTTON_TAP_ID = "btn_tap"
        const val BUTTON_DATA = "button_data"
        const val BUTTON_SELECTED_ID = "btn_id"
        const val SCENE_LOADS = "scene_loads"
        const val ROOMS_FRAGMENT = "room_fragment"
        const val ROOM_NAME = "room_name"
        const val REMOTE_DELETE = "remote_delete"
        const val CURTAIN = "Curtain"
        const val APPLIANCE_TYPE = "type"
        const val FAN_DIM = "fan_dim"
        const val CHANEL_CREATED = "create_channel"
        const val INTERNET_CHANNEL = "internet_channel"
        const val INTERNET_CHANNEL_LEARN = "learn_internet_channel"
        const val PREFS_LOCATION_NOT_REQUIRED = "location_not_required"
        const val PREFS_PERMISSION_REQUESTED = "permission_requested"
        const val BUTTON_DELETE = "button_delete"
        const val SET_BUTTON_ACTION = "button_action"
        const val RGB = "rgbLongPressed"
        const val RGB_TUNNABLE = "rgbTunnable"
        const val RGB_LOADS = "rgbLoads"
        const val TUNBLE_LOADS = "tunable_loads"
        const val CHANNEL_ADDED = "channel_added"
        const val AUTOMATION_ENABLE = "automation_enable"
        const val AUTOMATION_DELETE = "auto_delete"
        const val AUTOMATION_EDIT = " auto_edit"
        const val DEVICE_TITLE = "deviceTitle"
        const val SCENE_DELETE = "sceneDelete"
        const val REVOKE_ACCESS = "revoke_access"
        const val CURTAIN_OPEN = "curtain_open"
        const val CURTAIN_CLOSE = "curtain_close"
        const val CURTAIN_STOP = "curtain_stop"
        const val REMOTE_DATA = "remote_data"
        const val UPDATE_SCENE = "update_scene"
        const val REMOTE_SCENE_CONTROL = "remote_scene_control"

        const val MTN = 1
        const val LUX_VAL = 16
        const val MTN_LUX = 17
        const val TMP = 256
        const val TMP_MTN = 257
        const val TMP_LUX = 272
        const val TMP_LUX_MTN = 273
        const val HUM = 4096
        const val HUM_MTN = 4097
        const val HUM_LUX = 4112
        const val HUM_LUX_MTN = 4113
        const val HUM_TMP = 4352
        const val HUM_TMP_MTN = 4353
        const val HUM_TMP_LUX = 4368
        const val HUM_TMP_LUX_MTN = 4369
        const val MTN_DTCT = 5
        const val LUX_GTR = 80
        const val TEMP_GTR = 1280
        const val HUM_GTR = 20480
        const val BTN1_SINGL_TAP = "511A"
        const val BTN2_SINGL_TAP = "521A"
        const val BTN3_SINGL_TAP = "531A"
        const val BTN4_SINGL_TAP = "541A"
        const val BTN1_DOUBL_TAP = "512A"
        const val BTN2_DOUBL_TAP = "522A"
        const val BTN3_DOUBL_TAP = "532A"
        const val BTN4_DOUBL_TAP = "542A"
        const val BTN1_LONG_TAP = "513A"
        const val BTN2_LONG_TAP = "523A"
        const val BTN3_LONG_TAP = "533A"
        const val BTN4_LONG_TAP = "543A"
        const val REQUEST_ENABLE_GPS = 1000

        //Devices Name
        const val DEVICE_WOZART_SWITCH = "Wozart Switch Controller"
        const val DEVICE_WOZART_SWITCH_PRO = "Wozart Switch Controller Pro"
        const val DEVICE_WOZART_SENSE = "Wozart Sense"
        const val DEVICE_WOZART_PLUG = "Wozart Plug"
        const val DEVICE_WOZART_CURTAIN = "Wozart Motor Controller"
        const val DEVICE_LED = "Wozart LED Orchestrator"
        const val DEVICE_SWITCH_MINI = "Wozart Switch Controller Mini"
        const val DEVICE_WOZART_GATE = "Wozart Gate Controller"
        const val DEVICE_WOZART_DIMMER = "Wozart Dimmer"
        const val DEVICE_WOZART_FAN_CONTROLLER = "Wozart Fan Controller"
        const val DEVICE_UNIVERSAL_IR = "Wozart Universal Remote"

        const val DEVICE_WIFI_PREFIX = "Wozart"
        const val ERROR_SHOW = "error"
        const val DURING_THE_DAY = "During the day"
        const val AT_NIGHT = "At night"
        const val SPECIFIC_TIME = "Specific time"
        const val ANY_TIME = "Any time"
        const val DELETE_CAMERA = "camera_delete"
        const val VIDEO_PLAY = "video_play"
        const val VIDEO_STOPED = "video_stoped"
        const val UPDATE_SENSE_ROOM = "update_room"
        const val UPDATE_ROLE_SENSE = "sense_role"
        const val INSERT_NEW_SENSE = "insert_sense"
        const val CHANNEL_NAME = "channel_name"
        const val CHANNEL_NUMBER = "channel_number"
        const val UPGRADE_DEVICE = "upgrade_device"

        //Sense Loads Name
        const val MOTION_SENSOR = "Motion Sensor"
        const val TEMP_SENSOR = "Temperature Sensor"
        const val HUMIDITY_SENSOR = "Humidity Sensor"
        const val UNIVERSAL_REMOTE = "Universal Remote"
        const val LUX_SENSOR = "Lux Sensor"

        /*
        Permission Request
         */

        const val GALLERY = 1
        const val CAMERA = 2
        const val MY_CAMERA_PERMISSION_CODE = 100
        const val DATA_REQUEST = 1001

        /*
        ICON_VIEW_TYPE
         */
        const val ICON_VIEW = "icon_view"
        const val CHANNEL_ICON_VIEW = "channel_icon_view"

        /*
        TAB VALUE
         */
        const val INTERNET_CHANNEL_LIST = "Internet Channel"
        const val CHANNEL_SHORTCUT = "Channel Shortcut"


        /*
        Role Set for SENSE
         */
        const val MASTER = "master"
        const val SLAVE = "slave"


        /*
        * Device Upgrade URL
        */
        const val UPGRADE_SWITCH_URL = "https://wozart.s3.ap-south-1.amazonaws.com/WSC01.bin"
        const val UPGRADE_SWITCH_PRO_URL = "https://wozart.s3.ap-south-1.amazonaws.com/WSCP01.bin"
        const val UPGRADE_SWITCH_MINI_URL = "https://wozart.s3.ap-south-1.amazonaws.com/WSCM01.bin"
        const val UPGRADE_CURTAIN_URL = "https://wozart.s3.ap-south-1.amazonaws.com/WCC01.bin"
        const val UPGRADE_PLUG_URL = "https://wozart.s3.ap-south-1.amazonaws.com/WSP01.bin"
        const val UPGRADE_SENSE_URL = "https://wozart.s3.ap-south-1.amazonaws.com/WS01.bin"
        const val UPGRADE_SENSE_PRO_URL = "https://wozart.s3.ap-south-1.amazonaws.com/WSP01.bin"
        const val UPGRADE_LED_URL = "https://wozart.s3.ap-south-1.amazonaws.com/WLE01.bin"
        const val UPGRADE_MARVEL_SWITCH = "http://s3.ap-south-1.amazonaws.com/wozart/ota_android.bin"
        const val UPGRADE_TUNNABLE_URL = "https://wozart.s3.ap-south-1.amazonaws.com/WLE01_T.bin"
        const val UPGRADE_FIVE_NODE_SWITCH_URL = "https://wozart.s3.ap-south-1.amazonaws.com/WSC01_5N.bin"
        const val UPGRADE_FIVE_NODE_PUSH_SWITCH_URL = "https://wozart.s3.ap-south-1.amazonaws.com/WSC01_5N_PB.bin"
        const val UPGRADE_FAN_URL = "https://wozart.s3.ap-south-1.amazonaws.com/WFC01.bin"
        const val UPGRADE_FAN_PUSH_URL = "https://wozart.s3.ap-south-1.amazonaws.com/WFC01_PB.bin"
        const val UPGRADE_LED_PUSH_URL = "https://wozart.s3.ap-south-1.amazonaws.com/WLE01_PB.bin"
        const val UPGRADE_UNIVERSAL_IR = "https://wozart.s3.ap-south-1.amazonaws.com/WUR01.bin"
    }

}