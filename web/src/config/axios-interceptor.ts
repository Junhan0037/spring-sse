import axios, { AxiosError, AxiosInstance, AxiosResponse } from 'axios'

const http: AxiosInstance = axios.create({
  withCredentials: true,
  baseURL: '/',
})

http.defaults.headers.post['Content-Type'] = 'application/json'
http.defaults.headers.post['X-Requested-With'] = 'XMLHttpRequest'

http.interceptors.request.use(
  config => {
    // 요청을 보내기 전 수행할 작업
    return config
  },
  error => {
    // 오류 요청 가공
    return Promise.reject(error)
  }
)

http.interceptors.response.use(
  async (response: AxiosResponse): Promise<any> => {
    if (response.status >= 200 && response.status < 300) {
      return response.data
    }
  },
  async (error: AxiosError) => {
    let { response, request }: { response?: any; request?: XMLHttpRequest } =
      error
    if (response) {
      // 세션 만료 체크
      await handleSessionExpired(response)

      // Blob Response 인 경우 데이터 변환
      if (response.data instanceof Blob) {
        response.data = JSON.parse(await response.data.text())
      }

      // 에러 반환
      if (response.status >= 400 && response.status < 500) {
        console.log('error', response.status, response.data?.message)
        throw error
      }
    } else if (request) {
      console.log('Request failed. Please try again.', 'error')
      throw error
    }
    return Promise.reject(error)
  }
)

const handleSessionExpired = async (response: AxiosResponse) => {
  const { code, message } = response.data
  if (code === 'SECURITY001') {
    // 세션 만료 메시지
    window.location.href = '/'
    return
  }
}

export default http
