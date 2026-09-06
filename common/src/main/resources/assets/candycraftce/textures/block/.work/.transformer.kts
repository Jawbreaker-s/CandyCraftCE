import java.io.File

while (true) {
    print("type: ")
    val type = readln()
//val type = "chocolate_egg"
    val prefix = "${type}_"
    val suffix = ".png"
    val transformTemplate = "%s_${type}.png"
    val list = File(".").listFiles()
        .filter { it.name.startsWith(prefix) && it.name.endsWith(suffix) }
        .map {
            val name = it.name.run { substring(prefix.length, length - suffix.length) }
            it to transformTemplate.format(name)
        }
        .toList()
    list.forEach {
        println("${it.first.name}->\t${it.second}")
    }
    print("y/N: ")
    if (readln().lowercase() == "y") {
        list.forEach {
            it.first.renameTo(File(it.first.parentFile, it.second))
        }
    }
    println("=========================")
}