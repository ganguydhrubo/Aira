package com.ganguydhrubo.aira.media

import androidx.core.content.FileProvider

/** 把缓存里为播放复制出来的一份视频以只读 content:// 地址交给系统播放器;工作区本身不开放。 */
class VideoPlaybackFilesProvider : FileProvider()