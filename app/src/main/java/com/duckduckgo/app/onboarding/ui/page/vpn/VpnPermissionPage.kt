/*
 * Copyright (c) 2019 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.app.onboarding.ui.page.vpn

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.browser.databinding.ContentOnboardingVpnIntroBinding
import com.duckduckgo.app.browser.databinding.ContentOnboardingVpnPermissionBinding
import com.duckduckgo.app.global.FragmentViewModelFactory
import com.duckduckgo.app.onboarding.ui.page.OnboardingPageFragment
import com.duckduckgo.app.onboarding.ui.page.vpn.VpnPagesViewModel.Action
import com.duckduckgo.app.onboarding.ui.page.vpn.VpnPagesViewModel.Command.AskVpnPermission
import com.duckduckgo.app.onboarding.ui.page.vpn.VpnPagesViewModel.Command.ContinueToVpnExplanation
import com.duckduckgo.app.onboarding.ui.page.vpn.VpnPagesViewModel.Command.LeaveVpnIntro
import com.duckduckgo.app.onboarding.ui.page.vpn.VpnPagesViewModel.Command.LeaveVpnPermission
import com.duckduckgo.app.onboarding.ui.page.vpn.VpnPagesViewModel.Command.OpenVpnFAQ
import com.duckduckgo.di.scopes.FragmentScope
import com.duckduckgo.mobile.android.ui.view.addClickableLink
import com.duckduckgo.mobile.android.ui.viewbinding.viewBinding
import com.duckduckgo.mobile.android.vpn.ui.onboarding.DeviceShieldFAQActivity
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.content_onboarding_default_browser.*
import kotlinx.android.synthetic.main.include_default_browser_buttons.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@InjectWith(FragmentScope::class)
class VpnPermissionPage : OnboardingPageFragment() {

    @Inject
    lateinit var viewModelFactory: FragmentViewModelFactory

    private val binding: ContentOnboardingVpnPermissionBinding by viewBinding()

    private val viewModel: VpnPagesViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(VpnPagesViewModel::class.java)
    }

    override fun layoutResource(): Int = R.layout.content_onboarding_vpn_permission

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            viewModel.onAction(Action.PermissionPageBecameVisible)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel()
        setButtonsBehaviour()
        binding.onboardingPageText.addClickableLink(
            "learn_more_link",
            getText(com.duckduckgo.mobile.android.vpn.R.string.atp_daxOnboardingPermissionMessage)
        ) {
            viewModel.onAction(Action.LearnMore)
        }
    }

    private fun observeViewModel() {
        viewModel.commands()
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { processCommand(it) }
            .launchIn(lifecycleScope)
    }

    private fun processCommand(command: VpnPagesViewModel.Command) {
        when (command) {
            is AskVpnPermission -> onContinuePressed()
            is LeaveVpnPermission -> onOnboardingDone()
            is OpenVpnFAQ -> openFAQ()
        }
    }

    private fun openFAQ() {
        startActivity(DeviceShieldFAQActivity.intent(requireContext()))
    }

    private fun setButtonsBehaviour() {
        binding.onboardingMaybeLater.setOnClickListener {
            viewModel.onAction(Action.LeaveVpnPermission)
        }
        binding.onboardingNextCta.setOnClickListener {
            viewModel.onAction(Action.AskVpnPermission)
        }
    }
}
