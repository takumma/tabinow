data class SearchResponse(
    var YDF: YDF
)

data class YDF(
    var ResultInfo: ResultInfo,
    var Feature: List<Feature>
)

data class ResultInfo(
    var Count: Int? = null,
    var Total: Int? = null,
    var Start: Int? = null,
    var Status: Int? = null,
    var Description: Sting? = null,
    var Latency: Int? = null
)

data class Feature(
    var Id: Int? = null,
    var Gid: String? = null,
    var Name: String? = null,
    var Geometry: Geometry,
    var Category: List<Category>
)

data class Geometry(
    var Type: String? = null,
    var Coordinates: String? = null
)

data class Category(
    var List: list<List>//この辺分からん
)

data class List(
    var [String? = null]
)
//............................................................

data class WeatherApi(
    var cod: Int? = null,//エラー
    var message: Double? = null,
    var cnt: Int? = null,
    var list: ,
    var sys: sys,
    var dt_txt: String? = null //ここでいつの予報か見る
)

data class weather(
    var id: Int? = null,
    var main: String? = null,
    var description: String? = null,
    var icon: String? = null
)

data class list(
    var dt: Int? = null,
    var main: main,

    )

data class main(
    var twmp: Double? = null,
    var temp_min: Double? = null,
    var temp_max: Double? = null,
    var pressure: Double? = null,
    var sea_level: Double? = null,
    var grnd_level: Double? = null,
    var humidity: Int? = null,
    var temp_kf: Double? = null,
    var weather: List<weather>,
    var clouds: clouds,
    var wind: wind
)

data class sys(
    var pon: String? = null
)

data class clouds(
    var all: Int? = null
)

data class wind(
    var speed: Double? = null,
    var deg: Double? = null
)