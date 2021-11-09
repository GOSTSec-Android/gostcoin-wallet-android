package org.dash.wallet.integration.liquid.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import dagger.hilt.android.AndroidEntryPoint
import org.dash.wallet.common.services.analytics.AnalyticsConstants
import org.dash.wallet.common.services.analytics.AnalyticsService
import org.dash.wallet.common.services.analytics.FirebaseAnalyticsServiceImpl
import org.dash.wallet.integration.liquid.R
import org.dash.wallet.integration.liquid.data.LiquidConstants
import org.dash.wallet.integration.liquid.ui.WebViewActivity
import javax.inject.Inject

class CountrySupportDialog(val contexts: Context, val isCreditCard: Boolean, val analytics: AnalyticsService) : Dialog(contexts, R.style.Theme_Dialog) {

    init {
        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_crypto_support)

        findViewById<Button>(R.id.btnOkay).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                dismiss()
            }
        })

        findViewById<TextView>(R.id.txtCountrySupported).setOnClickListener {
            dismiss()
            analytics.logEvent(AnalyticsConstants.Liquid.SUPPORTED_COUNTRIES, bundleOf())
            val intent = Intent(contexts, WebViewActivity::class.java)
            intent.putExtra("url", LiquidConstants.COUNTRY_NOT_SUPPORTED)
            intent.putExtra("title", "Liquid")
            contexts.startActivity(intent)
        }

        findViewById<TextView>(R.id.buying_not_supported).apply {
            text = if (isCreditCard) {
                resources.getString(R.string.buying_dash_not_supported_credit_cards)
            } else {
                resources.getString(R.string.buying_dash_not_supported_crypto)
            }
        }

        findViewById<TextView>(R.id.payment_support).apply {
            text = if (isCreditCard) {
                resources.getString(R.string.credit_card_support)
            } else {
                resources.getString(R.string.crypto_support)
            }
        }

        findViewById<ImageView>(R.id.icon).apply {
            setImageDrawable(AppCompatResources.getDrawable(context, if (isCreditCard) {
                R.drawable.ic_creditcard
            } else {
                R.drawable.ic_cryptocurrency
            }))
        }
    }

}