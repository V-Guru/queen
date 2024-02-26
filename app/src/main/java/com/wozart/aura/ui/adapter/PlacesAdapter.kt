package com.yamikrish.app.exploremaplocation;

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.location.places.AutocompletePrediction
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


/**
 * Created by saif on 2/2/18.
 */
class PlacesAdapter(context: Context, resourceId: Int, geoData: PlacesClient, filter: AutocompletePrediction?, boundS_GREATER_SYDNEY: LatLngBounds) : ArrayAdapter<AutocompletePrediction>(context, resourceId), Filterable {

    var resultList: MutableList<AutocompletePrediction> = ArrayList()
    private val TAG = "PlaceAutoAdapter"
    val mContext = context
    val bounds = boundS_GREATER_SYDNEY
    val TASK_AWAIT = 120L


    val geoDataClient = geoData
    val mPlaceFilter = filter
    override fun getCount(): Int {
        return resultList.size
    }

    override fun getItem(position: Int): AutocompletePrediction? {
        return resultList.get(position)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = super.getView(position, convertView, parent!!)
        val item = getItem(position)
        val textView1 = row.findViewById<View>(android.R.id.text1) as TextView
        textView1.text = item?.getFullText(null)

        return row
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
                val results = Filter.FilterResults()

                var filterData: ArrayList<AutocompletePrediction>? = ArrayList()

                if (constraint != null) {
                   filterData = getAutocomplete(geoDataClient,constraint)
                }

                results.values = filterData
                if (filterData != null) {
                    results.count = filterData.size
                } else {
                    results.count = 0
                }

                return results
            }

            override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults?) {
                Log.v("results", "results==" + results);
                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    resultList = results.values as ArrayList<AutocompletePrediction>
                    Log.v("resultList", "resultList==" + resultList);
                    notifyDataSetChanged()
                } else {
                    // The API did not return any results, invalidate the data set.
                    notifyDataSetInvalidated()
                }
            }

            override fun convertResultToString(resultValue: Any): CharSequence {
                // Override this method to display a readable result in the AutocompleteTextView
                // when clicked.
                return if (resultValue is AutocompletePrediction) {
                    resultValue.getFullText(null)
                } else {
                    super.convertResultToString(resultValue)
                }
            }
        }
    }

    fun getAutocomplete(mPlacesClient: PlacesClient, constraint: CharSequence): ArrayList<AutocompletePrediction>? {
        var list = ArrayList<AutocompletePrediction>()
        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder().setTypeFilter(TypeFilter.ADDRESS).setSessionToken(token).setQuery(constraint.toString()).build()
        val prediction = mPlacesClient.findAutocompletePredictions(request)
        try {
            Tasks.await(prediction, TASK_AWAIT, TimeUnit.SECONDS)
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: TimeoutException) {
            e.printStackTrace()
        }

        if (prediction.isSuccessful) {
            val findAutocompletePredictionsResponse = prediction.result
            findAutocompletePredictionsResponse?.let {
                //list = findAutocompletePredictionsResponse!!.autocompletePredictions
            }
            return list
        }
        return list
    }

}