package cn.jawbreakers.candycraftce.utils


/*
 * An annotation only for notify developers that this function or class is only for client side.
 * When something is annotated with this annotation, it means that this function may not check whether it is on client side.
 *
 * 这个注解用于标记开发者这个函数或类仅用于客户端。
 * 当被此注解标注时，意味着这个函数可以不进行客户端检查。
 */
@Retention(AnnotationRetention.BINARY)
annotation class ClientOnly()
