# The denominator of the cache hit rate

If the global token totals match, but the hit rate of a session that mixes providers looks low, first check whether the model adapter still keeps the reporting status of the cache field. Some responses give only input and output; others clearly return zero cache reads. Once both are turned into the same integer, the old run summary cannot tell them apart.

The session panel adds up hit tokens per request, and counts in the denominator only the input of requests that explicitly reported cache use. When checking, mix three kinds of replies: hit, clear zero hit, and not reported. Input that was not reported should still add to the session's usage. Old summaries can fill in a lower bound for total usage; when coverage cannot be confirmed, keep it as unknown.

The regression entry points are `MeteredModelTest.conversationUsesReportedCacheDenominatorAndLatestMainContext` and `StreamingModelsTest.anthropicStreamTakesCumulativeUsageFromMessageDelta`. The first checks the weighting across different input lengths and side calls; the second checks that the streaming protocol tells apart zero hits from a missing field. This finding applies to metered inference requests within a session.

Verified once: the mixed-reporting unit test got 1,300 input tokens, of which the cache covered 400 and hit 80, a hit rate of 20%. A two-round scripted conversation on a OnePlus phone also verified the refresh while running and reopening after the end; that device test used fixed usage and was not connected to an online provider.
