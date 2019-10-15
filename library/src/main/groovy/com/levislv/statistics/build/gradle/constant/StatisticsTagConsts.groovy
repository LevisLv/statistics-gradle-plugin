package com.levislv.statistics.build.gradle.constant

/**
 * @author LevisLv
 * @email  levislv@levislv.com
 * @blog   https://blog.levislv.com/
 * @book   https://book.levislv.com/
 * @github https://github.com/LevisLv/
 */
interface StatisticsTagConsts {
    interface Page {
        int TAG_KEY_FOREGROUND = 100000001
        int TAG_KEY_ROOT_VIEW = 100000002
        int TAG_KEY_PKG_NAME = 100000003
        int TAG_KEY_PAGE_ID = 100000004
        int TAG_KEY_PAGE_NAME = 100000005
        int TAG_KEY_PAGE_DATA = 100000006
    }

    interface View {
        interface OnTouchListener {
            int TAG_KEY_MOTION_EVENT = 200010101
        }

        interface OnFocusChangeListener {
            int TAG_KEY_HAS_FOCUS = 200010201
        }
    }

    interface TextView {
        interface OnEditorActionListener {
            int TAG_KEY_ACTION_ID = 200020101
            int TAG_KEY_KEY_EVENT = 200020102
        }
    }

    interface CompoundButton {
        interface OnCheckedChangeListener {
            int TAG_KEY_IS_CHECKED = 200030101
        }
    }

    interface RadioGroup {
        interface OnCheckedChangeListener {
            int TAG_KEY_CHECKED_ID = 200040101
        }
    }

    interface SeekBar {
        interface OnSeekBarChangeListener {
            int TAG_KEY_PROGRESS = 200050101
            int TAG_KEY_FROM_USER = 200050102
        }
    }

    interface RatingBar {
        interface OnRatingBarChangeListener {
            int TAG_KEY_RATING = 200060101
            int TAG_KEY_FROM_USER = 200060102
        }
    }

    interface AdapterView {
        interface OnItemClickListener {
            int TAG_KEY_PARENT = 200070101
            int TAG_KEY_ITEM_POSITION = 200070102
            int TAG_KEY_ITEM_ID = 200070103
        }

        interface OnItemLongClickListener {
            int TAG_KEY_PARENT = 200070201
            int TAG_KEY_ITEM_POSITION = 200070202
            int TAG_KEY_ITEM_ID = 200070203
        }

        interface OnItemSelectedListener {
            int TAG_KEY_PARENT = 200070301
            int TAG_KEY_ITEM_POSITION = 200070302
            int TAG_KEY_ITEM_ID = 200070303
        }
    }

    interface ExpandableListView {
        interface OnGroupClickListener {
            int TAG_KEY_PARENT = 200080101
            int TAG_KEY_ITEM_POSITION = 200080102
            int TAG_KEY_ITEM_ID = 200080103
        }

        interface OnChildClickListener {
            int TAG_KEY_PARENT = 200080201
            int TAG_KEY_GROUP_POSITION = 200080202
            int TAG_KEY_CHILD_POSITION = 200080203
            int TAG_KEY_ITEM_ID = 200080204
        }
    }
}
