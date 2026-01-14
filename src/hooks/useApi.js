import { useState, useCallback } from 'react'
import toast from 'react-hot-toast'

/**
 * Custom hook for API calls with loading and error states
 */
export function useApi() {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const execute = useCallback(async (apiCall, options = {}) => {
    const {
      onSuccess,
      onError,
      successMessage,
      errorMessage,
      showSuccessToast = true,
      showErrorToast = true,
    } = options

    setLoading(true)
    setError(null)

    try {
      const response = await apiCall()
      const data = response.data

      if (showSuccessToast && successMessage) {
        toast.success(successMessage)
      }

      if (onSuccess) {
        onSuccess(data)
      }

      return { success: true, data }
    } catch (err) {
      const errorMsg = err.response?.data?.message || err.response?.data?.error || err.message || 'Une erreur est survenue'
      setError(errorMsg)

      if (showErrorToast) {
        toast.error(errorMessage || errorMsg)
      }

      if (onError) {
        onError(err)
      }

      return { success: false, error: errorMsg }
    } finally {
      setLoading(false)
    }
  }, [])

  const reset = useCallback(() => {
    setLoading(false)
    setError(null)
  }, [])

  return { loading, error, execute, reset }
}

/**
 * Hook for fetching data on mount
 */
export function useFetch(apiCall, dependencies = []) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const refetch = useCallback(async () => {
    setLoading(true)
    setError(null)

    try {
      const response = await apiCall()
      setData(response.data)
    } catch (err) {
      const errorMsg = err.response?.data?.message || err.message || 'Erreur de chargement'
      setError(errorMsg)
    } finally {
      setLoading(false)
    }
  }, dependencies)

  // Initial fetch
  useState(() => {
    refetch()
  })

  return { data, loading, error, refetch }
}

export default useApi
