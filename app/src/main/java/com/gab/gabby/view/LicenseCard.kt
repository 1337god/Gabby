/* Copyright 2018 Conny Duck
 *
 * This file is a part of Gabby.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Gabby is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Gabby; if not,
 * see <http://www.gnu.org/licenses>. */

package com.gab.gabby.view

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.card.MaterialCardView
import com.gab.gabby.R
import com.gab.gabby.util.LinkHelper
import com.gab.gabby.util.ThemeUtils
import com.gab.gabby.util.hide
import kotlinx.android.synthetic.main.card_license.view.*

class LicenseCard
@JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.card_license, this)

        setCardBackgroundColor(ThemeUtils.getColor(context, android.R.attr.colorBackground))

        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.LicenseCard, 0, 0)

        val name: String? = a.getString(R.styleable.LicenseCard_name)
        val license: String? = a.getString(R.styleable.LicenseCard_license)
        val link: String? = a.getString(R.styleable.LicenseCard_link)
        a.recycle()

        licenseCardName.text = name
        licenseCardLicense.text = license
        if(link.isNullOrBlank()) {
            licenseCardLink.hide()
        } else {
            licenseCardLink.text = link
            setOnClickListener { LinkHelper.openLink(link, context) }
        }

    }

}

