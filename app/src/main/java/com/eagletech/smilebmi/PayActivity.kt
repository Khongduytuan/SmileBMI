package com.eagletech.smilebmi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amazon.device.drm.LicensingService
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.FulfillmentResult
import com.amazon.device.iap.model.ProductDataResponse
import com.amazon.device.iap.model.PurchaseResponse
import com.amazon.device.iap.model.PurchaseUpdatesResponse
import com.amazon.device.iap.model.UserDataResponse
import com.eagletech.smilebmi.data.MyData
import com.eagletech.smilebmi.databinding.ActivityPayBinding

class PayActivity : AppCompatActivity() {
    private lateinit var sBinding: ActivityPayBinding
    private lateinit var myData: MyData
    private lateinit var currentUserId: String
    private lateinit var currentMarketplace: String

    // Phải thêm sku các gói vào ứng dụng
    companion object {
        const val bmi5 = "com.eagletech.smilebmi.calculatebmi5"
        const val bmi10 = "com.eagletech.smilebmi.calculatebmi10"
        const val bmi15 = "com.eagletech.smilebmi.calculatebmi15"
        const val sub = "com.eagletech.smilebmi.subcalculatebmi"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = ActivityPayBinding.inflate(layoutInflater)
        setContentView(sBinding.root)
        myData = MyData.getInstance(this)
        setupIAPOnCreate()
        setClickItems()

    }

    private fun setClickItems() {
        sBinding.btn5.setOnClickListener {
//            myData.addSaves(2)
            PurchasingService.purchase(bmi5)
        }
        sBinding.btn10.setOnClickListener {
            PurchasingService.purchase(bmi10)
        }
        sBinding.btn15.setOnClickListener {
            PurchasingService.purchase(bmi15)
        }
        sBinding.sub.setOnClickListener {
            PurchasingService.purchase(sub)
        }
        sBinding.finish.setOnClickListener { finish() }
    }

    private fun setupIAPOnCreate() {
        val purchasingListener: PurchasingListener = object : PurchasingListener {
            override fun onUserDataResponse(response: UserDataResponse) {
                when (response.requestStatus!!) {
                    UserDataResponse.RequestStatus.SUCCESSFUL -> {
                        currentUserId = response.userData.userId
                        currentMarketplace = response.userData.marketplace
                        myData.currentUserId(currentUserId)
                    }

                    UserDataResponse.RequestStatus.FAILED, UserDataResponse.RequestStatus.NOT_SUPPORTED -> Log.v(
                        "IAP SDK",
                        "loading failed"
                    )
                }
            }

            override fun onProductDataResponse(productDataResponse: ProductDataResponse) {
                when (productDataResponse.requestStatus) {
                    ProductDataResponse.RequestStatus.SUCCESSFUL -> {
                        val products = productDataResponse.productData
                        for (key in products.keys) {
                            val product = products[key]
                            Log.v(
                                "Product:", String.format(
                                    "Product: %s\n Type: %s\n SKU: %s\n Price: %s\n Description: %s\n",
                                    product!!.title,
                                    product.productType,
                                    product.sku,
                                    product.price,
                                    product.description
                                )
                            )
                        }
                        //get all unavailable SKUs
                        for (s in productDataResponse.unavailableSkus) {
                            Log.v("Unavailable SKU:$s", "Unavailable SKU:$s")
                        }
                    }

                    ProductDataResponse.RequestStatus.FAILED -> Log.v("FAILED", "FAILED")
                    else -> {}
                }
            }

            override fun onPurchaseResponse(purchaseResponse: PurchaseResponse) {
                when (purchaseResponse.requestStatus) {
                    PurchaseResponse.RequestStatus.SUCCESSFUL -> {

                        if (purchaseResponse.receipt.sku == bmi5) {
                            myData.addSaves(5)
                            finish()
                        }
                        if (purchaseResponse.receipt.sku == bmi10) {
                            myData.addSaves(10)
                            finish()
                        }
                        if (purchaseResponse.receipt.sku == bmi15) {
                            myData.addSaves(15)
                            finish()
                        }
                        if (purchaseResponse.receipt.sku == sub) {
                            myData.isPremiumSaves = true
                            finish()
                        }
                        PurchasingService.notifyFulfillment(
                            purchaseResponse.receipt.receiptId, FulfillmentResult.FULFILLED
                        )

                        Log.v("FAILED", "FAILED")
                    }

                    PurchaseResponse.RequestStatus.FAILED -> {}
                    else -> {}
                }
            }

            override fun onPurchaseUpdatesResponse(response: PurchaseUpdatesResponse) {
                // Process receipts
                when (response.requestStatus) {
                    PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL -> {
                        for (receipt in response.receipts) {
                            myData.isPremiumSaves = !receipt.isCanceled
                        }
                        if (response.hasMore()) {
                            PurchasingService.getPurchaseUpdates(false)
                        }

                    }

                    PurchaseUpdatesResponse.RequestStatus.FAILED -> Log.d("FAILED", "FAILED")
                    else -> {}
                }
            }
        }
        PurchasingService.registerListener(this, purchasingListener)
        Log.d(
            "DetailBuyAct", "Appstore SDK Mode: " + LicensingService.getAppstoreSDKMode()
        )
    }


    override fun onResume() {
        super.onResume()
        PurchasingService.getUserData()
        val productSkus: MutableSet<String> = HashSet()
        productSkus.add(sub)
        productSkus.add(bmi5)
        productSkus.add(bmi10)
        productSkus.add(bmi15)
        PurchasingService.getProductData(productSkus)
        PurchasingService.getPurchaseUpdates(false)
    }
}