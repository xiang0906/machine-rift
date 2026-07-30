// Lightweight narrative content kept on the frontend to avoid expanding the API or database.
const STAGE_STORIES = Object.freeze({
  '裂隙前線': '第一批機械群正穿越外圍裂隙。建立基礎防線，守住最後的城市入口。',
  '機械迴廊': '敵軍正利用廢棄輸送廊道快速推進。控制路線轉折點，阻止它們突破內環。',
  '核心裂谷': '裂谷能量持續強化重裝單位。撐過混合攻勢，切斷核心的能量供應。',
  '熔火交叉口': '熔爐區的管線在此交會，敵軍將沿曲折路徑湧入。精準配置火力，守住冷卻中樞。',
  '量子迷城': '量子干擾讓戰場訊號變得混亂。利用狹窄區域集中火力，摧毀核心護衛機群。',
  '機神核心': '裂隙源頭就在機神核心深處。擊退最終軍勢，永久關閉機械裂隙。',
});

function storyForStage(stageName) {
  return STAGE_STORIES[stageName]
    || '偵測到未知機械部隊正在接近。部署防禦塔，確保基地不被突破。';
}

function resultStoryForStage(stageName, result) {
  if (result === 'WIN' && stageName === '機神核心') {
    return '機神核心停止運轉，天空中的裂隙終於閉合。這座城市撐過了最後一夜。';
  }
  if (result === 'WIN') {
    return `「${stageName}」防線已穩定，裂隙能量正在消退。下一區域的座標已傳回指揮部。`;
  }
  return `「${stageName}」防線遭到突破，但戰場資料已保存。重新配置塔陣，再次阻止機械軍勢。`;
}
