package cn.jawbreakers.candycraftce.utils

import kotlin.reflect.KClass

/**
 * Created in 2026/9/22 12:59 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 * Description:
 * A marker that the class/field/method is used by mixin.
 */
@Retention(AnnotationRetention.BINARY)
annotation class UsedByMixin(val value: KClass<*> = Any::class)
