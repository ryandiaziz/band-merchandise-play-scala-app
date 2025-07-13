package models.keranjang

import play.api.libs.json.{Json, OFormat}

case class KeranjangWithItem(
    keranjangId: Int = -1,
    userId: Int,
    totalHarga: Double = 0,
    items: List[KeranjangItem]
)

object KeranjangWithItem {
  implicit val keranjangWithItemFormat: OFormat[KeranjangWithItem] = Json.format[KeranjangWithItem]
}