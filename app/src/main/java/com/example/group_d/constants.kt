package com.example.group_d
//collection names
const val COL_USER = "user"
const val COL_GAMES = "games"

//user keywords
const val USER_CHALLENGES = "challenges"
const val USER_FRIEND_REQUESTS = "friendRequests"
const val USER_FRIENDS = "friends"
const val USER_GAMES = "games"
const val USER_STATUS = "status"
const val USER_SEARCHING = "searching"
const val USER_NAME = "name"
const val USER_DATA = "userData"

//games keywords
const val GAME_BEGINNER = "beginner"
const val GAME_DATA = "gameData"
const val GAME_TYPE = "gameType"
const val GAME_TYPE_TIC_TAC_TOE = "TIC_TAC_TOE"
const val GAME_TYPE_COMPASS = "Compass"
const val GAME_PLAYERS = "players"

//compass constants
const val LOCATIONS_BASE_URL = "https://geoportal.kassel.de/arcgis/rest/services/Service_Daten/Freizeit_Kultur/MapServer/0/"
const val LOCATIONS_GET_QUERY = "query?where=1%3D1&text=&objectIds=&time=&geometry=&geometryType=esriGeometryEnvelope&inSR=&spatialRel=esriSpatialRelIntersects&distance=&units=esriSRUnit_Foot&relationParam=&outFields=*&returnGeometry=true&returnTrueCurves=false&maxAllowableOffset=&geometryPrecision=&outSR=&havingClause=&returnIdsOnly=false&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&gdbVersion=&historicMoment=&returnDistinctValues=false&resultOffset=&resultRecordCount=&returnExtentOnly=false&datumTransformation=&parameterValues=&rangeValues=&quantizationParameters=&featureEncoding=esriDefault&f=geojson"