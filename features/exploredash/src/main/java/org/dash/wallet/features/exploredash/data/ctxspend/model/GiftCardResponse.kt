/*
 * Copyright 2023 Dash Core Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dash.wallet.features.exploredash.data.ctxspend.model

import com.google.gson.annotations.SerializedName

data class GiftCardResponse(
    @SerializedName("id") val id: String,
    @SerializedName("status") val status: String,
    @SerializedName("barcodeUrl") val barcodeUrl: String? = "",
    @SerializedName("cardNumber") val cardNumber: String? = "",
    @SerializedName("cardPin") val cardPin: String? = "",

    @SerializedName("cryptoAmount") val cryptoAmount: String? = "",
    @SerializedName("cryptoCurrency") val cryptoCurrency: String? = "",
    @SerializedName("fiatAmount") val fiatAmount: String? = "",
    @SerializedName("fiatCurrency") val fiatCurrency: String? = "",
    @SerializedName("paymentUrls") val paymentUrls: Map<String, String>? = buildMap { },
)
