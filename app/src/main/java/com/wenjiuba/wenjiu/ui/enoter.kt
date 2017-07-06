package com.wenjiuba.wenjiu.ui

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wenjiuba.wenjiu.net.post
import kotlinx.android.synthetic.main.fragment_enoter_reports.view.*
import kotlinx.android.synthetic.main.fragment_new_enoter_report.view.*
import kotlinx.android.synthetic.main.item_enoter_report.view.*
import rx.subjects.PublishSubject
import java.util.*
import android.widget.ArrayAdapter
import com.rengwuxian.materialedittext.validation.RegexpValidator
import com.wenjiuba.wenjiu.*
import com.wenjiuba.wenjiu.net.get
import com.wenjiuba.wenjiu.util.DateUtil
import com.zzhoujay.richtext.RichText
import kotlinx.android.synthetic.main.fragment_enoter_report_detail.view.*
import java.math.BigDecimal


class EnoterReportsFragment : Fragment() {

    private var enoterReportsRecyclerAdapter: ListRecyclerAdapter<EnoterReport>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        enoterReportsRecyclerAdapter = ListRecyclerAdapter<EnoterReport>(R.layout.item_enoter_report, { enoterReport, view, _, _ ->
            //    view.case_item_title.text = case.title
            view.item_enoter_report_fullName.text = enoterReport.fullName

            view.item_enoter_report_paymentInd.text = enoterReport.paymentInd?.text
            view.item_enoter_report_createdAt.text = DateUtil.formatDate(Date(enoterReport.createdAt!!))

            view.item_enoter_report_requestPackageInd.text = enoterReport.requestPackageInd?.text
            view.item_enoter_robotReviewInd.text = enoterReport.robotReviewInd?.text
            view.item_enoter_expert1ReviewInd.text = enoterReport.expert1ReviewInd?.text

            // pay button if any
            val payButton = view.item_enoter_report_pay_button
            val isUnpaid = enoterReport.paymentInd == PaymentInd.UNPAID
            payButton.visibility =  if (isUnpaid) View.VISIBLE else View.GONE
            payButton.setOnClickListener {
                gotoPayment(activity, enoterReport)
            }

            // view button if any
            val viewButton = view.item_enoter_report_view_button
            val isFinishReview = (enoterReport.requestPackageInd == RequestPackageInd.ROBOT && enoterReport.robotReviewInd == ReviewInd.FINISHED) ||
                                 (enoterReport.requestPackageInd == RequestPackageInd.EXPERT && enoterReport.expert1ReviewInd == ReviewInd.FINISHED)

            viewButton.visibility = if(isFinishReview) View.VISIBLE else View.GONE
            viewButton.setOnClickListener {
                val args = Bundle()
                args.putString("enoterReport", Gson().toJson(enoterReport))
                val dialog = EnoterReportDetailFragment()
                dialog.arguments = args
                dialog.show(activity.getSupportFragmentManager(), "Enoter Report Detail")
            }

        }, "enoter/reports", null, object : TypeToken<List<EnoterReport>>() {}.type, false, false)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_enoter_reports, container, false)

        //recycler view
        view.enoter_reports_recycler.adapter = enoterReportsRecyclerAdapter
        view.enoter_reports_recycler.setLayoutManager(LinearLayoutManager(context));


        enoterReportsRecyclerAdapter!!.itemClicked.subscribe { enoterReport ->
            if (activity == null) return@subscribe



        }

        // pull to refresh
        val swipeRefreshLayout = view.enoter_reports_swipeRefreshLayout
        val refreshZone = view.enoter_reports_refreshing_zone
        swipeRefreshLayout.setProgressViewEndTarget(true, 120)
        swipeRefreshLayout.setDistanceToTriggerSync(300)
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        swipeRefreshLayout.setOnRefreshListener {
            refreshZone.visibility = View.VISIBLE

            enoterReportsRecyclerAdapter!!.refresh(null) {
                swipeRefreshLayout.isRefreshing = false
                refreshZone.visibility = View.GONE
            }
        }

        // load data for the first time
        swipeRefreshLayout.post { swipeRefreshLayout.setRefreshing(true) }
        refreshZone.visibility = View.VISIBLE
        enoterReportsRecyclerAdapter!!.refresh(null, {
            swipeRefreshLayout.setRefreshing(false)
            refreshZone.visibility = View.GONE
        })

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater!!.inflate(com.wenjiuba.wenjiu.R.menu.enoter_reports_menu, menu)
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_new_enoter_report) {
            val dialog = NewEnoterReportFragment()
            dialog.enoterReportAdded.subscribe { enoterReport ->
                enoterReportsRecyclerAdapter!!.add(enoterReport)
            }
            dialog.show(activity.supportFragmentManager, "New Enoter Report")
        }

        return super.onOptionsItemSelected(item)
    }

}

enum class Gender(val text: String) {
    MALE("男"),
    FEMALE("女")
}

enum class YesNo(val text: String) {
    YES("是"),
    NO("否")
}

enum class Menses(val text: String) {
    IMMATURE("未行经"),
    MENSES("行经"),
    CEASE("闭经")
}

enum class ProductType(val text: String) {
    ENOTER("e络通"),
    MOXIBUSTION("艾灸"),
    MISC("其它")
}

enum class TransactionType(val text: String) {
    ENOTER("e络通"),
    ALIPAY("支付宝")
}

enum class RequestPackageInd(val text: String) {
    ROBOT("机器人判读"),
    EXPERT("专家判读"),
//    FREE("免费")
}

enum class RequestExpertInd(val text: String) {
    USER("用户指定"),
    SYSTEM("系统指定")
}

enum class ReviewInd(val text: String) {
    PENDING("未开始"),
    INPROGRESS("进行中"),
    FINISHED("已完成")
}

enum class PaymentInd(val text: String) {
    UNPAID("未支付"),
    PAID("已支付"),
    EXCEPTION("支付异常")
}

class NewEnoterReportFragment : FullScreenDialogFragment() {
    val enoterReportAdded = PublishSubject.create<EnoterReport>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(context, R.layout.fragment_new_enoter_report, null)
        dialog.setContentView(view)

        val closeButton = view.new_enoter_report_close_button
        val summitButton = view.new_enoter_report_submit_button

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        // ----------

        // 默认套餐
        var enoterPackage: EnoterPackage? = null
        get("enoter/packages/default", mapOf(), { gson, json ->
            enoterPackage = gson.fromJson(json, EnoterPackage::class.java)
        }, {})

        // 性别和行经情况
        view.new_enoter_report_gender.setLabels(ArrayList<String>(Gender.values().map { gender -> gender.text }))
        view.new_enoter_report_menses.setLabels(ArrayList<String>(Menses.values().map { menses -> menses.text }))

        view.new_enoter_report_gender.setOnToggleSwitchChangeListener { position, isChecked ->
            view.new_enoter_report_menses.visibility = if (position == 1 && isChecked) View.VISIBLE else View.GONE
        }

        // 判读套餐和判读专家
        view.new_enoter_report_requestPackageInd.setLabels(ArrayList<String>(RequestPackageInd.values().map { pack -> pack.text }))
        view.new_enoter_report_requestPackageInd.setOnToggleSwitchChangeListener { position, isChecked ->
            view.new_enoter_report_expert1.visibility = if (position == 1 && isChecked) View.VISIBLE else View.GONE
            view.new_enoter_report_price_desc.text = if (position == 1 && isChecked) """总计：¥ ${enoterPackage?.expertPrice}元""" else """总计：¥ ${enoterPackage?.robotPrice}元"""
        }

        var experts = listOf<Expert>()
        get("experts", mapOf(), { gson, json ->
            experts = gson.fromJson(json, object : TypeToken<List<Expert>>() {}.type)
            val expertsAdapter = ArrayAdapter<String>(this.activity, R.layout.spinner_item, experts.map { expert -> expert.fullName })
            view.new_enoter_report_expert1.setAdapter(expertsAdapter)
        }, {})




        summitButton.setOnClickListener {
            summitButton.isEnabled = false


            // validation
            if (!validate(view)) {
                summitButton.isEnabled = true
                return@setOnClickListener
            }

            val enoterReport = EnoterReport()
            enoterReport.fullName = view.new_enoter_report_fullName.text.toString()
            enoterReport.age = view.new_enoter_report_age.text.toString().toInt()
            enoterReport.gender = Gender.values()[view.new_enoter_report_gender.checkedTogglePosition]
            enoterReport.menses = Menses.values()[view.new_enoter_report_menses.checkedTogglePosition]
            enoterReport.height = BigDecimal(view.new_enoter_report_height.text.toString())
            enoterReport.weight = BigDecimal(view.new_enoter_report_weight.text.toString())
            enoterReport.bpHigh = BigDecimal(view.new_enoter_report_bpHigh.text.toString())
            enoterReport.bpLow = BigDecimal(view.new_enoter_report_bpLow.text.toString())

            enoterReport.llu = BigDecimal(view.new_enoter_report_llu.text.toString())
            enoterReport.rlu = BigDecimal(view.new_enoter_report_rlu.text.toString())
            enoterReport.lpc = BigDecimal(view.new_enoter_report_lpc.text.toString())
            enoterReport.rpc = BigDecimal(view.new_enoter_report_rpc.text.toString())
            enoterReport.lht = BigDecimal(view.new_enoter_report_lht.text.toString())
            enoterReport.rht = BigDecimal(view.new_enoter_report_rht.text.toString())
            enoterReport.lsi = BigDecimal(view.new_enoter_report_lsi.text.toString())
            enoterReport.rsi = BigDecimal(view.new_enoter_report_rsi.text.toString())
            enoterReport.lte = BigDecimal(view.new_enoter_report_lte.text.toString())
            enoterReport.rte = BigDecimal(view.new_enoter_report_rte.text.toString())
            enoterReport.lli = BigDecimal(view.new_enoter_report_lli.text.toString())
            enoterReport.rli = BigDecimal(view.new_enoter_report_rli.text.toString())
            enoterReport.lsp = BigDecimal(view.new_enoter_report_lsp.text.toString())
            enoterReport.rsp = BigDecimal(view.new_enoter_report_rsp.text.toString())
            enoterReport.llr = BigDecimal(view.new_enoter_report_llr.text.toString())
            enoterReport.rlr = BigDecimal(view.new_enoter_report_rlr.text.toString())
            enoterReport.lki = BigDecimal(view.new_enoter_report_lki.text.toString())
            enoterReport.rki = BigDecimal(view.new_enoter_report_rki.text.toString())
            enoterReport.lbl = BigDecimal(view.new_enoter_report_lbl.text.toString())
            enoterReport.rbl = BigDecimal(view.new_enoter_report_rbl.text.toString())
            enoterReport.lgb = BigDecimal(view.new_enoter_report_lgb.text.toString())
            enoterReport.rgb = BigDecimal(view.new_enoter_report_rgb.text.toString())
            enoterReport.lst = BigDecimal(view.new_enoter_report_lst.text.toString())
            enoterReport.rst = BigDecimal(view.new_enoter_report_rst.text.toString())

            enoterReport.requestPackageInd = RequestPackageInd.values()[view.new_enoter_report_requestPackageInd.checkedTogglePosition]
            enoterReport.expert1 = experts.findLast { it.fullName === view.new_enoter_report_expert1.text.toString() }?.id

            post("enoter/reports", enoterReport, { gson, json ->
                val enoterReportCreated = gson.fromJson<EnoterReport>(json, EnoterReport::class.java)
                Toast.makeText(context, "保存判读成功", Toast.LENGTH_LONG).show()
                enoterReportAdded.onNext(enoterReportCreated)

                // 打开订单确认页面
                if (enoterReportCreated.paymentInd == PaymentInd.UNPAID) {
                    gotoPayment(activity, enoterReportCreated)
                }

                summitButton.isEnabled = true
                dialog.dismiss()
            }, {
                summitButton.isEnabled = true
            })
        }

        return dialog
    }

    fun validate(view: View): Boolean {
        val notEmptyValidator = RegexpValidator("不能为空", """\w{2,10}""")
        val intNumberValidator = RegexpValidator("必须为整数", """\d{1,10}""")
        val decimalNumberValidator = RegexpValidator("必须为整数或两位小数", """(0|[1-9]\d+)(.\d{1,2})?""")

        var isValid = true
        isValid = view.new_enoter_report_fullName.validateWith(notEmptyValidator) && isValid
        isValid = view.new_enoter_report_age.validateWith(intNumberValidator) && isValid
        isValid = view.new_enoter_report_height.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_weight.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_bpHigh.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_bpLow.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_llu.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_rlu.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_lpc.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_rpc.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_lht.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_rht.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_lsi.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_rsi.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_lte.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_rte.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_lli.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_rli.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_lsp.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_rsp.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_llr.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_rlr.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_lki.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_rki.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_lbl.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_rbl.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_lgb.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_rgb.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_lst.validateWith(decimalNumberValidator) && isValid
        isValid = view.new_enoter_report_rst.validateWith(decimalNumberValidator) && isValid
        if(RequestPackageInd.values()[view.new_enoter_report_requestPackageInd.checkedTogglePosition] == RequestPackageInd.EXPERT) {
            isValid = view.new_enoter_report_expert1.validateWith(notEmptyValidator) && isValid
        }

        return isValid
    }
}

fun gotoPayment(activity: FragmentActivity, enoterReport: EnoterReport) {
    if (enoterReport.paymentInd == PaymentInd.PAID) return

    get("alipay/presubmit", mapOf("businessType" to "ENOTER_REPORT", "businessId" to enoterReport.id.toString()), { gson, json ->
        //                        val alipayTrade = gson.fromJson(json, AlipayTrade::class.java)
        val args = Bundle()
        args.putString("alipayTrade", json)//Gson().toJson(alipayTrade))
        val dialog = PaymentFragment()
        dialog.arguments = args
        dialog.show(activity.getSupportFragmentManager(), "Enoter Report Payment")
    }, {})
}




class EnoterReportDetailFragment : FullScreenDialogFragment() {
    var enoterReport: EnoterReport? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enoterReport = Gson().fromJson(arguments.getString("enoterReport"), EnoterReport::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(context, R.layout.fragment_enoter_report_detail, null)
        dialog.setContentView(view)

        view.enoter_report_detail_fullName.text = enoterReport?.fullName
        RichText.fromHtml(enoterReport?.robotReport).into(view.enoter_report_detail_robotReport)

        view.enoter_report_detail_close_button.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }
}