package com.alancamilo.airparis

import airparis.base.BaseViewModel
import airparis.base.Coordinator
import airparis.base.State
import airparis.base.StateChangeListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

/**
 * Base [Fragment] used to map a [BaseViewModel] and handle its lifecycle events.
 */
open class BaseFragment<CD : Coordinator, ST : State, VM : BaseViewModel<CD, ST>, out B : ViewDataBinding> :
    Fragment(), StateChangeListener<ST> {

    private var isBindingInitialized: Boolean = false
    private var isInitialized: Boolean = false
    @LayoutRes
    private var bindingLayoutId: Int = 0
    private var actionsBindingVariableId: Int = 0
    private var stateBindingVariableId: Int = 0

    protected lateinit var binding: ViewDataBinding
    protected lateinit var viewModel: VM
    private var state: ST? = null

    /**
     * Initialize all DataBinding variables.
     */
    protected fun initialize(
        @LayoutRes bindingLayoutId: Int,
        @IdRes actionsBindingVariableId: Int,
        @IdRes stateBindingVariableId: Int,
        vm: VM
    ) {
        isInitialized = true
        this.bindingLayoutId = bindingLayoutId
        this.actionsBindingVariableId = actionsBindingVariableId
        this.stateBindingVariableId = stateBindingVariableId
        viewModel = vm
        viewModel.setStateChangeListener(this)
        this as? CD
            ?: throw UninitializedPropertyAccessException("Fragment does not implement the base.Coordinator interface!")
        viewModel.setCoordinator(this as CD)
    }

    override fun onStateChange(state: ST) {
        this.state = state
        if (isBindingInitialized)
            binding.setVariable(stateBindingVariableId, state)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        instanceState: Bundle?
    ): View? {
        if (!isInitialized)
            throw UninitializedPropertyAccessException("initialize() not called!")
        binding = DataBindingUtil.inflate<B>(inflater, bindingLayoutId, container, false)
        binding.setVariable(actionsBindingVariableId, viewModel)
        binding.setVariable(stateBindingVariableId, state)
        isBindingInitialized = true
        return binding.root
    }

    //region base.BaseViewModel lifecycle events
    override fun onStart() {
        super.onStart()
        viewModel.onActive()
    }

    override fun onStop() {
        viewModel.onInactive()
        super.onStop()
    }

    override fun onDestroy() {
        viewModel.onDestroy()
        super.onDestroy()
    }
    //endregion
}