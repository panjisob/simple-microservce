<template>
  <div class="container">
    <h1>📰 Reactive News Feed</h1>

    <div v-for="news in newsList" :key="news.id" class="news-item">
      <h3>{{ news.title }}</h3>
      <p>{{ news.content }}</p>
      <small>{{ formatDate(news.createdAt) }}</small>
    </div>

    <div v-if="loading" class="loading">Loading...</div>
    <div v-if="noMore" class="end">No more news.</div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import axios from 'axios'

const newsList = ref([])
const cursor = ref(null)
const limit = 10
const loading = ref(false)
const noMore = ref(false)

const loadNews = async () => {
  if (loading.value || noMore.value) return
  loading.value = true
  try {
    const response = await axios.get('http://localhost:8080/v1/news', {
      params: {
        cursor: cursor.value,
        limit,
      },
    })

    const newItems = response.data
    if (newItems.length === 0) {
      noMore.value = true
    } else {
      newsList.value.push(...newItems)
      cursor.value = newItems[newItems.length - 1].createdAt
    }
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const handleScroll = () => {
  const scrollPos = window.innerHeight + window.scrollY
  const bottom = document.body.offsetHeight - 100

  if (scrollPos >= bottom) {
    loadNews()
  }
}

onMounted(() => {
  loadNews()
  window.addEventListener('scroll', handleScroll)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
})

const formatDate = (isoString) => new Date(isoString).toLocaleString()
</script>

<style>
body {
  font-family: sans-serif;
}
.container {
  max-width: 600px;
  margin: auto;
  padding: 1rem;
}
.news-item {
  padding: 1rem;
  border-bottom: 1px solid #ccc;
}
.loading {
  text-align: center;
  padding: 1rem;
}
.end {
  text-align: center;
  color: gray;
  padding: 1rem;
}
</style>
