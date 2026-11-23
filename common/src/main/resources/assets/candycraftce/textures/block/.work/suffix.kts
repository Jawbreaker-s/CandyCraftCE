import java.io.File

val toc = File(".").listFiles { it.name.endsWith(".png") }.map {
    val name = it.name
//    if ("_top_" in name) {
//        it to it.name.replace("_top_", "_").replace(".png", "_top.png")
//    } else {
//        it to it.name.replace(".png", "_side.png")
//    }
    if ("_top" in name) {
        it to it.name.replace("_top", "_end")
    } else {
        null
    }
}.filterNotNull()

toc.forEach {
    println(it.first.name + "\t" + it.second)
}

readln()

toc.forEach {
    it.first.renameTo(File(it.first.parent, it.second))
}