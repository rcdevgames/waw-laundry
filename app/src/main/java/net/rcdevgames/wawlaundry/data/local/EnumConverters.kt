package net.rcdevgames.wawlaundry.data.local

import androidx.room.TypeConverter
import net.rcdevgames.wawlaundry.data.local.entity.DeliveryType
import net.rcdevgames.wawlaundry.data.local.entity.OrderStatus
import net.rcdevgames.wawlaundry.data.local.entity.PaymentMethod
import net.rcdevgames.wawlaundry.data.local.entity.PaymentStatus
import net.rcdevgames.wawlaundry.data.local.entity.PromoType

class EnumConverters {

    @TypeConverter
    fun fromPromoType(value: PromoType): String = value.name

    @TypeConverter
    fun toPromoType(value: String): PromoType = enumValueOf(value)

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus): String = value.name

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus = enumValueOf(value)

    @TypeConverter
    fun fromOrderStatus(value: OrderStatus): String = value.name

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus = enumValueOf(value)

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = enumValueOf(value)

    @TypeConverter
    fun fromDeliveryType(value: DeliveryType): String = value.name

    @TypeConverter
    fun toDeliveryType(value: String): DeliveryType = enumValueOf(value)
}
