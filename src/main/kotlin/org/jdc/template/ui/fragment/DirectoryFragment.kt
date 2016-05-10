package org.jdc.template.ui.fragment

import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.view.ActionMode
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.google.android.gms.analytics.HitBuilders
import kotlinx.android.synthetic.main.fragment_directory.*
import org.jdc.template.Analytics
import org.jdc.template.App
import org.jdc.template.R
import org.jdc.template.event.*
import org.jdc.template.inject.Injector
import org.jdc.template.model.database.main.individual.Individual
import org.jdc.template.model.database.main.individual.IndividualConst
import org.jdc.template.model.database.main.individual.IndividualManager
import org.jdc.template.ui.adapter.DirectoryAdapter
import pocketbus.Bus
import pocketbus.Subscribe
import pocketbus.SubscriptionRegistration
import pocketbus.ThreadMode
import pocketknife.BindArgument
import pocketknife.PocketKnife
import pocketknife.SaveState
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

class DirectoryFragment : BaseFragment(), SearchView.OnQueryTextListener, ActionMode.Callback {

    @Inject
    lateinit var bus: Bus
    @Inject
    lateinit var analytics: Analytics
    @Inject
    lateinit var individualManager: IndividualManager

    @BindArgument(ARGS_DUAL_PANE)
    var dualPane = false

    @SaveState
    var lastSelectedId: Long = 0

    lateinit var adapter: DirectoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PocketKnife.bindArguments(this)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newFloatingActionButton.setOnClickListener() {
            onNewItemClick()
        }
    }

    override val layoutResourceId: Int
        get() = R.layout.fragment_directory

    init {
        Injector.get().inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        PocketKnife.restoreInstanceState(this, savedInstanceState)
        setHasOptionsMenu(true)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = DirectoryAdapter(activity, dualPane)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        // setup the right edge border
        recyclerView.setBackgroundResource(if (dualPane) R.drawable.listview_dual_background else 0)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        lastSelectedId = adapter.lastSelectedItemId

        PocketKnife.saveInstanceState(this, outState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.directory_menu, menu)

        val searchMenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = MenuItemCompat.getActionView(searchMenuItem) as SearchView
        searchView.queryHint = getString(R.string.menu_search_hint)
        searchView.setOnQueryTextListener(this)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
        loadList()
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    fun loadList() {
        val observable = individualManager.findBySelectionRx(null, null, IndividualConst.C_FIRST_NAME + ", " + IndividualConst.C_LAST_NAME)
                .subscribeOn(Schedulers.io())
                .toList()
                .observeOn(AndroidSchedulers.mainThread())

        addSubscription(observable.subscribe { data -> dataLoaded(data) })
    }

    fun dataLoaded(data: List<Individual>) {
        if (dualPane) {
            selectPosition(lastSelectedId)
        }

        adapter.set(data);
    }

    @Subscribe(ThreadMode.MAIN)
    fun handleDirectoryItemClickedEvent(event: DirectoryItemClickedEvent) {
        selectPosition(event.itemId)
    }

    @Subscribe(ThreadMode.MAIN)
    fun handleIndividualSavedEvent(event: IndividualSavedEvent) {
        loadList()
        bus.post(DirectoryItemSelectedEvent(event.id))
    }

    @Subscribe(ThreadMode.MAIN)
    fun handleIndividualDeletedEvent(event: IndividualDeletedEvent) {
        loadList()
    }

    override fun onQueryTextSubmit(s: String): Boolean {
        Log.i(TAG, "Search Submit: " + s)
        return true
    }

    override fun onQueryTextChange(s: String): Boolean {
        Log.i(TAG, "Search Change: " + s)
        return true
    }

    override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        val inflater = actionMode.menuInflater
        inflater.inflate(R.menu.directory_menu, menu)
        return true
    }

    override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
        return false
    }

    override fun onDestroyActionMode(actionMode: ActionMode) {
        // destroy action mode
    }

    private fun selectPosition(id: Long) {
        // Only if we're showing both fragments should the item be "highlighted"
        if (dualPane) {
            adapter.lastSelectedItemId = id
        }

        bus.post(DirectoryItemSelectedEvent(id))
    }

    fun onNewItemClick() {
        analytics.send(HitBuilders.EventBuilder().setCategory(Analytics.CATEGORY_INDIVIDUAL).setAction(Analytics.ACTION_NEW).build())

        bus.post(EditIndividualEvent())
    }

    companion object {
        val TAG = App.createTag(DirectoryFragment::class.java)

        const val ARGS_DUAL_PANE = "DUAL_PANE"

        fun newInstance(dualPane: Boolean): DirectoryFragment {
            val fragment = DirectoryFragment()
            val args = Bundle()
            args.putBoolean(ARGS_DUAL_PANE, dualPane)
            fragment.arguments = args
            return fragment
        }
    }
}
